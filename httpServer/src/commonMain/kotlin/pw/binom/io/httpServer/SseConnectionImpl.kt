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

  override suspend fun send(event: String?, data: String) {
    require('\n' !in data) { NEWLINE_ERROR }
    sendEvent(event)
    sendStart()
    output.append(data)
    sendEnd()
  }

  override suspend fun send(event: String?, data: AsyncReader) {
    sendEvent(event)
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
