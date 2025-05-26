package pw.binom.http.client

import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncReader
import pw.binom.io.StreamClosedException
import pw.binom.io.http.Headers

class SseConnectionImpl(
  override val responseCode: Int,
  override val responseHeaders: Headers,
  private var output: AsyncCloseable,
  private var input: AsyncReader,
) : SseConnection {
  private suspend fun readEmptyLine(){
    val line = input.readln() ?: throw StreamClosedException()
    check(line.isEmpty()){"Invalid SSE protocol: line after payload should be empty"}
  }
  override suspend fun <T> read(read: (String?, String) -> T): T {
    val line = input.readln() ?: throw StreamClosedException()

    if (line.startsWith("event: ")) {
      val eventName = line.substring(7)
      val line = input.readln() ?: throw StreamClosedException()
      check(line.startsWith("data: ")) { "Invalid payload data: $line" }
      val payload = line.substring(6)
      readEmptyLine()
      return read(eventName, payload)
    }
    if (line.startsWith("data:")) {
      val payload = line.substring(6)
      readEmptyLine()
      return read(null, payload)
    }
    throw IllegalStateException("Invalid event data: $line")
  }

  override suspend fun asyncClose() {
    input.asyncClose()
    output.asyncCloseAnyway()
  }
}
