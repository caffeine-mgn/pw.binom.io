package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.crypto.Sha1MessageDigest
import pw.binom.io.IOException
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.InvalidSecurityKeyException
import pw.binom.io.http.websocket.WebSocketConnectionImpl
import pw.binom.url.URL

class WebSocketRequest(
  override val url: URL,
  override val method: String,
  val masking: Boolean,
  val client: HttpClient,
) : RequestConfig {
  override val headers = HashHeaders2()
  private var started = false

  suspend fun start(bufferSize: Int = DEFAULT_BUFFER_SIZE): SuccessWebSocketConnection {
    check(!started) { "Connection already started" }
    started = true
    val requestKey = HandshakeSecret.generateRequestKey()
    val responseKey = HandshakeSecret.generateResponse(Sha1MessageDigest(), requestKey)
    headers[Headers.SEC_WEBSOCKET_KEY] = requestKey

    val request = client.startConnect(method = method, uri = url, headers = headers, keepAlive = false)
    val resp = request.flush()
    if (resp.responseCode != 101) {
      throw IOException("Invalid Response code: ${resp.responseCode}")
    }
    val respKey = resp.inputHeaders.getSingleOrNull(Headers.SEC_WEBSOCKET_ACCEPT)
      ?: throw IOException("Invalid Server Response. Missing header \"${Headers.SEC_WEBSOCKET_ACCEPT}\"")
    if (respKey != responseKey) {
      throw InvalidSecurityKeyException()
    }
    return SuccessWebSocketConnection(
      connection = WebSocketConnectionImpl(
        _input = request.input,
        _output = request.output,
        masking = masking,
        bufferSize = bufferSize,
        mainChannel = request.mainChannel,
      ),
      headers = HashHeaders2(resp.inputHeaders),
    )
  }
}

fun HttpClient.connectWebSocket(
  uri: URL,
  headers: Headers = emptyHeaders(),
  masking: Boolean = true,
  method: String = "GET",
): WebSocketRequest {
  val wsRequest = WebSocketRequest(
    client = this,
    url = uri,
    method = method,
    masking = masking,
  )
  wsRequest.headers[Headers.HOST] = uri.host + (uri.port?.let { ":$it" } ?: "")
  wsRequest.headers[Headers.CONNECTION] = Headers.UPGRADE
  wsRequest.headers[Headers.UPGRADE] = Headers.WEBSOCKET
  wsRequest.headers[Headers.SEC_WEBSOCKET_VERSION] = "13"
  wsRequest.headers.add(headers)
  return wsRequest
}
