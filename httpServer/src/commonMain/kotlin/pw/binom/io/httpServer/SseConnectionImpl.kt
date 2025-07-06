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

  override suspend fun send(eventName: String?, id: String, retry: String?, data: String) {
    require('\n' !in data) { NEWLINE_ERROR }
    sendEvent(eventName)
    sendId(id)
    sendRetry(retry)
    sendStart()
    output.append(data)
    sendEnd()
  }

  override suspend fun send(eventName: String?, id: String, retry: String?, data: AsyncReader) {
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
  }

  suspend fun flush() {
    output.flush()
  }

  override suspend fun asyncClose() {
    output.asyncClose()
    mainChannel.asyncClose()
  }
}
