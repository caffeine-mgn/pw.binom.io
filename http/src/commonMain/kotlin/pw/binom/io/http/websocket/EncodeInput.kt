package pw.binom.io.http.websocket

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.*

class EncodeInput(
  val stream: AsyncInput,
  private var mask: Int,
) : AsyncInput {
  override val available: Int
    get() = -1

  private var cursor = 0L

  fun reset(mask: Int) {
    cursor = 0L
    this.mask = mask
  }


  private val buffer = ByteBuffer(DEFAULT_BUFFER_SIZE).empty()

  override suspend fun read(dest: ByteBuffer): DataTransferSize {
    if (!dest.hasRemaining) {
      return DataTransferSize.EMPTY
    }
    if (!buffer.hasRemaining) {
      buffer.clear()
      val result = stream.read(buffer)
      if (result.isAvailable) {
        buffer.flip()
        buffer.holdState {
          MessageCoder.encode(
            cursor = cursor,
            mask = mask,
            data = buffer,
          )
        }
      }
    }
    return buffer.read(dest)
  }

  override suspend fun asyncClose() {
    buffer.close()
    stream.asyncClose()
  }
}
