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
  private suspend fun readEmptyLine() {
    val line = input.readln() ?: throw StreamClosedException()
    check(line.isEmpty()) { "Invalid SSE protocol: line after payload should be empty" }
  }

  override suspend fun <T> read(read: (String?, String) -> T): T {
    var bc = false
    var eventName: String? = null
    var id: String? = null
    var retry: String? = null
    val sb = StringBuilder()
    var payloadExist = false
    var blank = true
    while (true) {
      val line = input.readln() ?: throw StreamClosedException()
      if (line.startsWith(":")) {
        continue
      }
      println("READING LINE: $line")
      if (line == "bc") {
        bc = true
        blank = false
        continue
      }
      if (line.startsWith("event: ")) {
        eventName = line.substring(7)
        blank = false
        continue
      }
      if (line.startsWith("id: ")) {
        id = line.substring(4)
        blank = false
        continue
      }
      if (line.startsWith("retry: ")) {
        retry = line.substring(7)
        blank = false
        continue
      }
      if (line.startsWith("data: ")) {
        if (payloadExist) {
          sb.append("\n")
        }
        sb.append(line.substring(6))
        payloadExist = true
        blank = false
        continue
      }
      if (line.isEmpty()) {
        if (blank) {
          throw StreamClosedException()
        } else {
          val payload = sb.toString()
          sb.clear()
          bc = false
          eventName = null
          id = null
          payloadExist = false
          blank = true
          return read(eventName, payload)
        }
      }
//      if (line.startsWith("event: ")) {
//        val eventName = line.substring(7)
//        val line = input.readln() ?: throw StreamClosedException()
//        check(line.startsWith("data: ")) { "Invalid payload data: $line" }
//        val payload = line.substring(6)
//        readEmptyLine()
//        return read(eventName, payload)
//      }
//      if (line.startsWith("data:")) {
//        val payload = line.substring(6)
//        readEmptyLine()
//        return read(null, payload)
//      }
      throw IllegalStateException("Invalid event data: $line")
    }
  }

  override suspend fun asyncClose() {
    input.asyncClose()
    output.asyncCloseAnyway()
  }
}
