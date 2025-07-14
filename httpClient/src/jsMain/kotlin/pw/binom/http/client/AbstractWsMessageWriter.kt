package pw.binom.http.client

import pw.binom.io.AsyncOutput
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize

abstract class AbstractWsMessageWriter : AsyncOutput {
  private val buffer = ByteArrayOutput()
  private var closed = false

  override suspend fun write(data: ByteBuffer): DataTransferSize {
    if (closed) {
      DataTransferSize.CLOSED
    }
    if (!data.hasRemaining) {
      return DataTransferSize.EMPTY
    }
    return buffer.write(data)
  }

  protected abstract fun send(buffer: ByteBuffer)
  private fun internalFlush() {
    if (buffer.size <= 0) {
      return
    }
    buffer.locked {
      send(it)
    }
    buffer.clear()
  }

  override suspend fun asyncClose() {
    if (!closed) {
      closed = true
      internalFlush()
    }
  }

  override suspend fun flush() {
    if (closed) {
      return
    }
    internalFlush()
  }
}
