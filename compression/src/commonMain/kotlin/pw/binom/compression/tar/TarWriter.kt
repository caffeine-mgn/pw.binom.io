package pw.binom.compression.tar

import pw.binom.io.*
import pw.binom.set

private val ZERO_BYTE = ByteBuffer(100).also {
  while (it.remaining > 0) {
    it.put(0)
  }
}

fun ByteBuffer.writeZero() {
  var s = remaining
  while (s > 0) {
    ZERO_BYTE.reset(0, minOf(ZERO_BYTE.capacity, s))
    val b = write(ZERO_BYTE)
    if (b.isNotAvailable) {
      throw RuntimeException("No space for write zero")
    }
    s -= b.length
  }
}

internal fun Int.forPart(partSize: Int): Int {
  var fullSize = (this / partSize) * partSize
  if (this % partSize > 0) {
    fullSize += partSize
  }
  return fullSize
}

private fun Output.writeZero(size: Int) {
  if (size == 0) {
    return
  }
  require(size > 0)
  var s = size
  while (s > 0) {
    ZERO_BYTE.reset(0, minOf(ZERO_BYTE.capacity, s))
    val b = write(ZERO_BYTE)
    if (b.isAvailable) {
      s -= b.length
    }
  }
}

internal fun UShort.toOct(dst: ByteBuffer, dstOffset: Int, size: Int) {
  this.toLong().toOct(dst, dstOffset, size)
}

internal fun UInt.toOct(dst: ByteBuffer, dstOffset: Int, size: Int) {
  this.toLong().toOct(dst, dstOffset, size)
}

internal fun Long.toOct(dst: ByteBuffer, dstOffset: Int, size: Int) {
  var n = this

  var len = 0
  while (n != 0L) {
    n = n shr 3
    len++
  }
  n = this

  (dstOffset until (dstOffset + size - len)).forEach {
    dst[it] = '0'.code.toByte()
  }
  var i = 0
  while (n != 0L) {
    val v = (n and 0x7).toByte()
    val value = (v + '0'.code.toByte()).toByte()
    val index = dstOffset + (size - 2) - i
    dst[index] = value
    n = n shr 3
    i++
  }
  dst[dstOffset + (size - 1)] = 0
}

private val magic = byteArrayOf(
  'u'.code.toByte(),
  's'.code.toByte(),
  't'.code.toByte(),
  'a'.code.toByte(),
  'r'.code.toByte(),
  0,
)
private val version = byteArrayOf(
  '0'.code.toByte(),
  '0'.code.toByte(),
)

private val longLink = "././@LongLink".encodeToByteArray()

class TarWriter(val stream: Output, val closeStream: Boolean = true) : Closeable {

  private var entityWriting = false

  private abstract class AbstractOutput(
    val block: ByteBuffer,
    val stream: Output,
    val closeListener: () -> Unit,
  ) : Output {
    protected abstract val dateSize: Long
    protected open fun closeCheck() {
    }

    protected open fun beforeZeros() {
    }

    protected open fun beforeClose() {
    }

    override fun close() {
      closeCheck()
      dateSize.toOct(block, 124, 12)

      block.calcCheckSum().toOct(block, 148, 7)
      block[155] = ' '.code.toByte()
      stream.writeFully(block)
      block.close()
      beforeZeros()

      val mod = (dateSize % BLOCK_SIZE).toInt()
      if (mod > 0) {
        val emptyBytes = BLOCK_SIZE - mod
        stream.writeZero(emptyBytes)
      }
      closeListener()
      beforeClose()
    }
  }

  private class KnownSizeOutput(
    block: ByteBuffer,
    stream: Output,
    val size: Long,
    closeListener: () -> Unit,
  ) : AbstractOutput(closeListener = closeListener, block = block, stream = stream) {
    private var wrote = 0L

    override val dateSize: Long
      get() = size

    override fun write(data: ByteBuffer): DataTransferSize {
      require(wrote + data.remaining <= size) { "Real data size should be equals defined size before" }
      val l = stream.write(data)
      if (l.isAvailable) {
        wrote += l.length
      }
      return l
    }

    override fun flush() {
      stream.flush()
    }

    override fun closeCheck() {
      check(wrote == size) { "Real data size should be equals defined size before" }
    }
  }

  private class UnknownSizeOutput(
    block: ByteBuffer,
    stream: Output,
    closeListener: () -> Unit,
  ) : AbstractOutput(closeListener = closeListener, block = block, stream = stream) {
    private val data = ByteArrayOutput()

    override fun write(data: ByteBuffer) = this.data.write(data)

    override fun flush() {
      data.flush()
    }

    override val dateSize: Long
      get() = data.size.toLong()

    override fun beforeZeros() {
      data.flush()
      data.locked {
        stream.writeFully(it)
      }
    }

    override fun beforeClose() {
      data.close()
    }
  }

  private fun finishEntityWrite() {
    entityWriting = false
  }

  fun newEntity(
    name: String,
    mode: UShort,
    uid: UShort,
    gid: UShort,
    time: Long,
    type: TarEntityType,
    dataSize: Long?,
  ): Output {
    checkFinished()
    check(!entityWriting) { "You mast close previous Entity" }
    entityWriting = true
    val block = ByteBuffer(BLOCK_SIZE)

    name.encodeToByteArray().wrap { nameBytes ->
      if (nameBytes.capacity > 100) {
        longLink.copyInto(block)
        mode.toOct(block, 100, 8)
        uid.toOct(block, 108, 8)
        gid.toOct(block, 116, 8)
        time.toOct(block, 136, 12)

        block[156] = 76
        magic.copyInto(block, 257)
        version.copyInto(block, 263)
        (nameBytes.capacity + 1).toUInt().toOct(block, 124, 12)

        block.calcCheckSum().toOct(block, 148, 7)
        block[155] = ' '.code.toByte()
        stream.write(block)
        stream.write(nameBytes)
        val fullSize = (nameBytes.capacity + 1).forPart(BLOCK_SIZE.toInt())
        val needAddZero = fullSize - nameBytes.capacity
        stream.writeZero(needAddZero)
        block.clear()
        block.writeZero()
        nameBytes.reset(0, 100)
        block.clear()
        block.write(nameBytes)
      } else {
        block.write(nameBytes)
      }
    }
    block.clear()

    mode.toOct(block, 100, 8)
    uid.toOct(block, 108, 8)
    gid.toOct(block, 116, 8)
    time.toOct(block, 136, 12)

    block[156] = type.num
    magic.copyInto(block, 257)
    version.copyInto(block, 263)
    return if (dataSize == null) {
      UnknownSizeOutput(
        block = block,
        stream = stream,
        closeListener = this::finishEntityWrite,
      )
    } else {
      KnownSizeOutput(
        block = block,
        stream = stream,
        size = dataSize,
        closeListener = this::finishEntityWrite,
      )
    }
  }

  var isFinished = false
    private set

  private inline fun checkFinished() {
    check(!isFinished) { "TarWrite already finished" }
  }

  override fun close() {
    checkFinished()
    check(!entityWriting) { "You mast close previous Entity" }
    stream.writeZero(BLOCK_SIZE)
    stream.writeZero(BLOCK_SIZE)
    isFinished = true
    stream.flush()
    if (closeStream) {
      stream.close()
    }
  }
}

internal fun ByteBuffer.calcCheckSum(): UInt {
  var chksum = 0u
  (position until limit).forEach {
    chksum += this[it].toUInt()
  }
  chksum += 256u
  return chksum
}

/**
 * Copies this array or its subrange into the [destination] ByteBuffer and returns that ByteBuffer.
 *
 *
 * @param destination the ByteBuffer to copy to.
 * @param destinationOffset the position in the [destination] ByteBuffer to copy to, 0 by default.
 * @param startIndex the beginning (inclusive) of the subrange to copy, 0 by default.
 * @param endIndex the end (exclusive) of the subrange to copy, size of this array by default.
 *
 * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of this array indices or when `startIndex > endIndex`.
 * @throws IndexOutOfBoundsException when the subrange doesn't fit into the [destination] array starting at the specified [destinationOffset],
 * or when that index is out of the [destination] array indices range.
 *
 * @return the [destination] ByteBuffer.
 */
internal fun ByteArray.copyInto(
  destination: ByteBuffer,
  destinationOffset: Int = 0,
  startIndex: Int = 0,
  endIndex: Int = size,
): ByteBuffer {
  destination.set(destinationOffset, endIndex - startIndex) {
    it.write(this, startIndex, endIndex - startIndex)
  }
  return destination
}
