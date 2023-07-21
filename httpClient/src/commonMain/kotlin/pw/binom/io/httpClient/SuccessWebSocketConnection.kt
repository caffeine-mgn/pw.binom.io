package pw.binom.io.httpClient

import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.WebSocketConnection

data class SuccessWebSocketConnection(
  val connection: WebSocketConnection,
  val headers: Headers,
) : WebSocketConnection by connection
