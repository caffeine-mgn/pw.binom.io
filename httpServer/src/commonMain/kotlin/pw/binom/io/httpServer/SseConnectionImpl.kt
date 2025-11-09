package pw.binom.io.httpServer

import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncReader
import pw.binom.io.AsyncWriter
import pw.binom.io.EOFException

internal class SseConnectionImpl(
  private val mainChannel: AsyncCloseable,
  private var output: AsyncWriter,
) : SseConnection {
  companion object {
    private const val NEWLINE_ERROR = "data should not contains new line chars"
  }

  private suspend fun sendStart() {
    output.append("data: ")
  }

  private suspend fun sendEnd() {
    output.append("\n\n")
  }

  private suspend fun sendEvent(event: String?) {
    event ?: return
    require('\n' !in event) { NEWLINE_ERROR }
    output.append("event: ").append(event).append("\n")
  }

  private suspend fun sendId(id: String?) {
    id ?: return
    require('\n' !in id) { NEWLINE_ERROR }
    output.append("id: ").append(id).append("\n")
  }

  private suspend fun sendRetry(retry: String?) {
    retry ?: return
    require('\n' !in retry) { NEWLINE_ERROR }
    output.append("retry: ").append(retry).append("\n")
  }

  override suspend fun send(data: String?, eventName: String?, id: String?, retry: String?) {
    if (data != null) {
      require('\n' !in data) { NEWLINE_ERROR }
    }
    sendEvent(eventName)
    sendId(id)
    sendRetry(retry)
    sendStart()
    if (data != null) {
      output.append(data)
    }
    sendEnd()
    output.flush()
  }

  override suspend fun send(data: AsyncReader, eventName: String?, id: String?, retry: String?) {
    sendEvent(eventName)
    sendId(id)
    sendRetry(retry)
    sendStart()
    while (true) {
      try {
        val char = data.readChar() ?: break
        if (char == '\n') {
          throw IllegalArgumentException(NEWLINE_ERROR)
          sendEnd()
        }
      } catch (_: EOFException) {
        break
      }
    }
    sendEnd()
    output.flush()
  }

  suspend fun flush() {
    output.flush()
  }

  override suspend fun asyncClose() {
    output.asyncClose()
    mainChannel.asyncClose()
  }
}
