package pw.binom.io.http

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.StreamClosedException
import pw.binom.io.UTF8

/**
 * Implements Async Http Chunked Transport Output
 *
 * @param stream real output stream
 * @param autoFlushBuffer size of buffer for auto flush data in buffer
 * @param closeStream flag for auto close [stream] when this stream will close
 */
open class AsyncChunkedOutput(
  stream: AsyncOutput,
  private val autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE,
  closeStream: Boolean = false,
) : AsyncOutput {
  var stream: AsyncOutput = stream
    protected set
  var closeStream: Boolean = closeStream
    protected set
  protected val closed = AtomicBoolean(false)
  protected val finished = AtomicBoolean(false)
  protected val buffer = ByteBuffer(autoFlushBuffer)

  private val tmp = ByteBuffer(50)

  override suspend fun write(data: ByteBuffer): Int {
    ensureOpen()
    val len = data.remaining
    while (true) {
      if (data.remaining == 0) {
        break
      }
      if (buffer.remaining == 0) {
        buffer.flip()
        sendBuffer()
      }
      buffer.write(data)
    }
    return len
  }

  private suspend fun sendBuffer() {
    tmp.clear()
    UTF8.unicodeToUtf8((buffer.remaining).toString(16), tmp)
    tmp.put(CR)
    tmp.put(LF)
    tmp.flip()
    val wasSent = tmp.remaining + buffer.remaining
    stream.writeFully(tmp) // send data.length + CR + LF
    stream.writeFully(buffer) // send data
    tmp.clear()
    tmp.put(CR)
    tmp.put(LF)
    tmp.flip()
    stream.writeFully(tmp) // send CR + LF
    stream.flush()
    buffer.clear()
  }

  private suspend fun internalFlush() {
    if (buffer.position == 0) {
      return
    }
    buffer.flip()
    sendBuffer()
  }

  override suspend fun flush() {
    ensureOpen()
    internalFlush()
  }

  private suspend fun finish() {
    if (!finished.compareAndSet(false, true)) {
      return
    }
    internalFlush()
    tmp.clear()
    tmp.put('0'.code.toByte())
    tmp.put(CR)
    tmp.put(LF)
    tmp.put(CR)
    tmp.put(LF)
    tmp.flip()
    stream.write(tmp)

    stream.flush()
  }

  protected open fun closeInternalBuffers() {
    tmp.close()
    buffer.close()
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    try {
      finish()
      if (closeStream) {
        stream.asyncClose()
      }
    } finally {
      closeInternalBuffers()
    }
  }

  protected fun ensureOpen() {
    if (closed.getValue()) {
      throw StreamClosedException()
    }
  }
}
