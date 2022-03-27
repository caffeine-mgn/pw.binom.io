package pw.binom.compression.tar

import pw.binom.ByteBuffer
import pw.binom.Output
import pw.binom.io.ByteArrayOutput
import pw.binom.io.Closeable
import pw.binom.set
import pw.binom.wrap

private val ZERO_BYTE = ByteBuffer.alloc(100).also {
    while (it.remaining > 0)
        it.put(0)
}

fun ByteBuffer.writeZero() {
    var s = remaining
    while (s > 0) {
        ZERO_BYTE.reset(0, minOf(ZERO_BYTE.capacity, s))
        val b = write(ZERO_BYTE)
        if (b == 0) {
            throw RuntimeException("No space for write zero")
        }
        s -= b
    }
}


internal fun Int.forPart(partSize: Int): Int {
    var fullSize = (this / partSize) * partSize
    if (this % partSize > 0)
        fullSize += partSize
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
        s -= b
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
    0
)
private val version = byteArrayOf(
    '0'.code.toByte(),
    '0'.code.toByte()
)

private val longLink = "././@LongLink".encodeToByteArray()

class TarWriter(val stream: Output, val closeStream: Boolean = true) : Closeable {

    private var entityWriting = false

    fun newEntity(name: String, mode: UShort, uid: UShort, gid: UShort, time: Long, type: TarEntityType): Output {
        checkFinished()
        if (entityWriting)
            throw IllegalStateException("You mast close previous Entity")
        entityWriting = true
        val block = ByteBuffer.alloc(BLOCK_SIZE)

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
                var fullSize = (nameBytes.capacity + 1).forPart(BLOCK_SIZE.toInt())
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

        return object : Output {
            val data = ByteArrayOutput()

            override fun write(data: ByteBuffer): Int =
                this.data.write(data)

            override fun flush() {
                data.flush()
            }

            override fun close() {
                data.size.toUInt().toOct(block, 124, 12)

                block.calcCheckSum().toOct(block, 148, 7)
                block[155] = ' '.code.toByte()
                stream.writeFully(block)
                block.close()
                data.flush()
                data.locked {
                    stream.writeFully(it)
                }

                val mod = data.size % BLOCK_SIZE
                if (mod > 0) {
                    val emptyBytes = BLOCK_SIZE - mod
                    stream.writeZero(emptyBytes)
                }
                entityWriting = false
                data.close()
            }
        }
    }

    var isFinished = false
        private set

    private inline fun checkFinished() {
        if (isFinished)
            throw IllegalStateException("TarWrite already finished")
    }

    override fun close() {
        checkFinished()
        if (entityWriting)
            throw IllegalStateException("You mast close previous Entity")
        stream.writeZero(BLOCK_SIZE.toInt())
        stream.writeZero(BLOCK_SIZE.toInt())
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
    endIndex: Int = size
): ByteBuffer {
    destination.set(destinationOffset, endIndex - startIndex) {
        it.write(this, startIndex, endIndex - startIndex)
    }
    return destination
}