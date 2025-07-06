package pw.binom.http.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charsets
import pw.binom.io.AsyncCloseable
import pw.binom.io.bufferedReader
import pw.binom.io.http.Headers
import pw.binom.io.useAsync
import kotlin.coroutines.coroutineContext

interface SSEEvent {
  val eventName: String?
  val id: String?
  val retry: String?
  val data: String?
}

interface SseConnection : AsyncCloseable {
  val responseCode: Int
  val responseHeaders: Headers
  suspend fun <T> read(read: (String?, String) -> T): T
}

private class SSEEventImpl : SSEEvent {
  override var eventName: String? = null
  override var id: String? = null
  override var retry: String? = null
  override var data: String? = null
  fun clear() {
    eventName = null
    id = null
    retry = null
    data = null
  }
}

fun Http11ClientExchange.sseFlow(bufferSize: Int = DEFAULT_BUFFER_SIZE): Flow<SSEEvent> {
  //  val charset = getResponseHeaders().getSingleOrNull(Headers.CHARSET)?.let { Charsets.get(it) } ?: Charsets.UTF8
  return flow<SSEEvent> {
    val event = SSEEventImpl()
    val sb = StringBuilder()
    var payloadExist = false
    var newEvent = true
    getInput().bufferedReader(charset = Charsets.UTF8, bufferSize = bufferSize).useAsync { reader ->
      while (coroutineContext.isActive) {
        val line = reader.readln() ?: break
        if (line == "bc") {
          newEvent = false
          continue
        }
        if (line.startsWith(":")) {
          continue
        }
        if (line.startsWith("event: ")) {
          event.eventName = line.substring(7)
          newEvent = false
          continue
        }
        if (line.startsWith("id: ")) {
          event.id = line.substring(4)
          newEvent = false
          continue
        }
        if (line.startsWith("retry: ")) {
          event.retry = line.substring(7)
          newEvent = false
          continue
        }
        if (line.startsWith("data: ")) {
          if (payloadExist) {
            sb.append("\n")
          }
          payloadExist = true
          sb.append(line.substring(6))
          newEvent = false
          continue
        }
        if (line.isEmpty()) {
          if (newEvent) {
            break
          } else {
            event.data = sb.toString()
            sb.clear()
            emit(event)
            event.clear()
            newEvent = true
            payloadExist = false
          }
          continue
        }
        throw IllegalStateException("Invalid string: \"$line\"")
      }
    }
  }
}

suspend fun Http11ClientExchange.toSSE(bufferSize: Int = DEFAULT_BUFFER_SIZE): SseConnection {
  val responseCode = getResponseCode()
  val responseHeaders = getResponseHeaders()
  val (input, output) = toRaw()
  val charset = responseHeaders.getSingleOrNull(Headers.CHARSET)?.let { Charsets.get(it) } ?: Charsets.UTF8
  val reader = input.bufferedReader(charset = charset, bufferSize = bufferSize)
  return SseConnectionImpl(
    responseCode = responseCode,
    responseHeaders = responseHeaders,
    input = reader,
    output = output,
  )
}
