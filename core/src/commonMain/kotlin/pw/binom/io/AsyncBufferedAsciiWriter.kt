package pw.binom.io

import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.atomic.AtomicBoolean

abstract class AbstractAsyncBufferedAsciiWriter(
  val closeParent: Boolean,
) : AsyncWriter, BufferedAsyncOutput {
  protected abstract val output: AsyncOutput
  protected abstract val buffer: ByteBuffer
  private var closed = AtomicBoolean(false)
  private val busyFlag = AtomicBoolean(false)

  private inline fun <T> busy(func: () -> T): T {
    if (!busyFlag.compareAndSet(false, true)) {
      TODO()
    }
    return try {
      func()
    } finally {
      busyFlag.setValue(false)
    }
  }

  protected fun afterConstruct() {
    buffer.clear()
  }

  protected open fun throwClosed(): Nothing = throw StreamClosedException()

  private fun ensureOpen() {
    if (closed.getValue()) {
      throwClosed()
    }
  }

  private suspend fun checkFlush() {
    if (buffer.remaining == 0) {
      flush()
    }
  }

  override val outputBufferSize: Int
    get() = buffer.capacity

  suspend fun write(
    data: ByteArray,
    offset: Int = 0,
    length: Int = data.size - offset,
  ): Int {
    busy {
      ensureOpen()
      var r = 0
      var remaining = length
      while (remaining > 0) {
        checkFlush()
        val wrote = buffer.write(data, offset = offset, length = length)
        r += wrote
        remaining -= wrote
      }
      return r
    }
  }

  override suspend fun write(data: ByteBuffer): DataTransferSize {
    busy {
      ensureOpen()
      var r = 0
      while (data.remaining > 0) {
        checkFlush()
        val l = buffer.write(data)
        if (l.isAvailable) {
          r += l.length
        }
      }
      return DataTransferSize.ofSize(r)
    }
  }

  override suspend fun append(value: Char): AsyncAppendable {
    busy {
      ensureOpen()
      checkFlush()
      buffer.put(value.code.toByte())
      return this
    }
  }

  override suspend fun append(value: CharSequence?): AsyncAppendable {
    value ?: return this
    append(value, 0, value.length)
    return this
  }

  override suspend fun append(
    value: CharSequence?,
    startIndex: Int,
    endIndex: Int,
  ): AsyncAppendable {
    ensureOpen()
    value ?: return this
    if (value.isEmpty()) {
      return this
    }
    if (endIndex == startIndex) {
      return append(value[startIndex])
    }
    busy {
      val data =
        ByteArray(endIndex - startIndex) {
          value[it].code.toByte()
        }
      var pos = 0
      while (pos < data.size) {
        checkFlush()
        val wrote = buffer.write(data, offset = pos)
        if (wrote <= 0) {
          throw IOException("Can't append data to")
        }
        pos += wrote
      }
      return this
    }
  }

  private suspend fun internalFlush() {
    if (buffer.position > 0) {
      buffer.flip()
      output.writeFully(buffer)
      buffer.clear()
      output.flush()
    }
  }

  override suspend fun flush() {
    ensureOpen()
    internalFlush()
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    internalFlush()
    buffer.close()
    if (closeParent) {
      output.asyncClose()
    }
  }
}

class AsyncBufferedAsciiWriter private constructor(
  override val output: AsyncOutput,
  override val buffer: ByteBuffer,
  closeParent: Boolean = true,
) : AbstractAsyncBufferedAsciiWriter(closeParent = closeParent) {
  override fun toString(): String = "AsyncBufferedAsciiWriter(output=$output)"

  constructor(output: AsyncOutput, pool: ByteBufferPool, closeParent: Boolean = true) : this(
    output = output,
    buffer = pool.borrow().clean(),
    closeParent = closeParent,
  )

  constructor(output: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE, closeParent: Boolean = true) : this(
    output = output,
    buffer = ByteBuffer(bufferSize).clean(),
    closeParent = closeParent,
  )

  init {
    afterConstruct()
  }
}

fun AsyncOutput.bufferedAsciiWriter(
  pool: ByteBufferPool,
  closeParent: Boolean = true,
) = AsyncBufferedAsciiWriter(
  output = this,
  pool = pool,
  closeParent = closeParent,
)

fun AsyncOutput.bufferedAsciiWriter(
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  closeParent: Boolean = true,
) = AsyncBufferedAsciiWriter(
  output = this,
  bufferSize = bufferSize,
  closeParent = closeParent,
)
