package pw.binom.io.http

import pw.binom.atomic.AtomicBoolean
import pw.binom.io.AsyncInput
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException
import pw.binom.io.DataTransferSize

open class AsyncContentLengthInput(
  val stream: AsyncInput,
  val contentLength: ULong,
  val closeStream: Boolean = false,
) : AsyncHttpInput {

  override val isEof: Boolean
    get() = eof

  private val eof
    get() = closed.getValue() || readed >= contentLength

  override val available: Int
    get() = minOf(contentLength - readed, Int.MAX_VALUE.toULong()).toInt()

  private var readed = 0uL
  private var closed = AtomicBoolean(false)

  override suspend fun read(dest: ByteBuffer): DataTransferSize {
    ensureOpen()
    if (dest.remaining == 0) {
      return DataTransferSize.EMPTY
    }
    if (eof) {
      return DataTransferSize.EMPTY
    }
    val read = if ((contentLength - readed < dest.remaining.toULong())) {
      val oldLimit = dest.limit
      val limit = contentLength - readed
      dest.limit = limit.toInt()
      val read = stream.read(dest)
      dest.limit = oldLimit
      read
    } else {
      stream.read(dest)
    }
    if (read.isAvailable) {
      readed += read.length.toULong()
    }
    return read
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    if (!isEof) {
      skipAll()
    }
    if (closeStream) {
      stream.asyncClose()
    }
  }

  override suspend fun skipAll(buffer: ByteBuffer) {
    if (closed.getValue()) {
      return
    }
    super.skipAll(buffer)
  }

  protected fun ensureOpen() {
    if (closed.getValue()) {
      throw ClosedException()
    }
  }
}
