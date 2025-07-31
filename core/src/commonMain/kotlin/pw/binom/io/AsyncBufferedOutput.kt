package pw.binom.io

import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE

class AsyncBufferedOutput2(
  override val stream: AsyncOutput,
  val pool: ByteBufferPool,
  private val closeStream: Boolean,
) : AbstractAsyncBufferedOutput() {
  override val buffer = pool.borrow()

  override suspend fun asyncClose() {
    try {
      super.asyncClose()
    } finally {
      if (closeStream) {
        stream.asyncClose()
      }
      pool.recycle(buffer)
    }
  }
}

class AsyncBufferedOutput(
  override val stream: AsyncOutput,
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  private val closeStream: Boolean,
) : AbstractAsyncBufferedOutput() {
  override val buffer = ByteBuffer(bufferSize)

  override suspend fun asyncClose() {
    try {
      super.asyncClose()
    } finally {
      if (closeStream) {
        stream.asyncClose()
      }
      buffer.close()
    }
  }
}

abstract class AbstractAsyncBufferedOutput : AsyncOutput, AsyncWriter {
  protected abstract val stream: AsyncOutput
  protected abstract val buffer: ByteBuffer
  private var closed = false
  val bufferSize
    get() = buffer.capacity

  private var internalWroteBytes = 0L
  val readBytes
    get() = internalWroteBytes

  private fun checkClosed() {
    if (closed) {
      throw StreamClosedException()
    }
  }

  override suspend fun append(value: CharSequence?): AsyncAppendable {
    value?.forEach {
      append(it)
    }
    return this
  }

  override suspend fun append(value: Char): AsyncAppendable {
    writeByte(value.code.toByte())
    return this
  }

  override suspend fun append(
    value: CharSequence?,
    startIndex: Int,
    endIndex: Int,
  ): AsyncAppendable {
    value ?: return this
    for (i in startIndex until endIndex) {
      append(value[i])
    }
    return this
  }

  suspend fun writeFully(data: ByteArray): Int {
    checkClosed()
    var l = 0
    while (l < data.size) {
      if (buffer.remaining <= 0) {
        flush()
      }
      val wrote = buffer.write(data)
      internalWroteBytes += wrote.length
      l += wrote.length
    }
    return l
  }

  override suspend fun write(data: ByteBuffer): DataTransferSize {
    checkClosed()
    var l = 0
    while (data.hasRemaining) {
      if (!buffer.hasRemaining) {
        flush()
      }
      val wrote = buffer.write(data)
      if (wrote.isAvailable) {
        internalWroteBytes += wrote.length
        l += wrote.length
      }
    }
    return DataTransferSize.ofSize(l)
  }

  override suspend fun writeByte(value: Byte) {
    if (!buffer.hasRemaining) {
      flush()
    }
    buffer.put(value)
    internalWroteBytes++
  }

  suspend fun writeByteArray(data: ByteArray) {
    data.forEach { // TODO оптимизировать
      writeByte(it)
    }
  }

  protected val hasDataForFlush
    get() = buffer.remaining != buffer.capacity

  protected open suspend fun sendDataToStream() {
    buffer.flip()
    stream.writeFully(buffer)
    stream.flush()
    buffer.clear()
  }

  protected suspend fun flushWithoutCloseCheck(): Boolean {
    if (!hasDataForFlush) {
      return false
    }
    sendDataToStream()
    return true
  }

  override suspend fun flush() {
    checkClosed()
    flushWithoutCloseCheck()
  }

  override suspend fun asyncClose() {
    checkClosed()
    flush()
    closed = true
  }
}

fun AsyncOutput.bufferedOutput(
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
  closeStream: Boolean = true,
): AsyncBufferedOutput {
  if (this is AsyncBufferedOutput && this.bufferSize == bufferSize) {
    return this
  }
  return AsyncBufferedOutput(
    stream = this,
    bufferSize = bufferSize,
    closeStream = closeStream,
  )
}

fun AsyncOutput.bufferedOutput(
  pool: ByteBufferPool,
  closeStream: Boolean = true,
): AsyncBufferedOutput2 {
  if (this is AsyncBufferedOutput2 && this.pool === pool) {
    return this
  }
  return AsyncBufferedOutput2(
    stream = this,
    pool = pool,
    closeStream = closeStream,
  )
}
