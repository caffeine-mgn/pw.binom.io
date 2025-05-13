package pw.binom.http.client

import pw.binom.crypto.Sha1MessageDigest
import pw.binom.io.AsyncChannel
import pw.binom.io.IOException
import pw.binom.io.http.*
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.InvalidSecurityKeyException
import pw.binom.io.http.websocket.WebSocketConnectionImpl
import pw.binom.io.httpClient.SuccessWebSocketConnection
import pw.binom.url.URL

class WsRequestBuilder(
  val client: HttpClientCommon,
  val headers: MutableHeaders,
  var method: String,
  var bufferSize: Int,
  var url: URL,
  var masking: Boolean,
) {
  suspend fun connect(): SuccessWebSocketConnection {
    val requestKey = HandshakeSecret.generateRequestKey()
    val responseKey = HandshakeSecret.generateResponse(Sha1MessageDigest(), requestKey)
    val resultHeaders = HashHeaders2()
    resultHeaders.addAll(headers)
    resultHeaders[Headers.SEC_WEBSOCKET_KEY] = requestKey
    resultHeaders[Headers.CONNECTION] = Headers.UPGRADE
    resultHeaders[Headers.UPGRADE] = Headers.WEBSOCKET
    resultHeaders[Headers.SEC_WEBSOCKET_VERSION] = "13"
    val exchange = client.connect(
      method = method,
      url = url,
      headers = resultHeaders,
    ) as Http11ClientExchange
    if (exchange.getResponseCode() != 101) {
      throw IOException("Invalid Response code: ${exchange.getResponseCode()}")
    }
    val respKey = exchange.getResponseHeaders().getSingleOrNull(Headers.SEC_WEBSOCKET_ACCEPT)
      ?: throw IOException("Invalid Server Response. Missing header \"${Headers.SEC_WEBSOCKET_ACCEPT}\"")
    if (respKey != responseKey) {
      throw InvalidSecurityKeyException()
    }
    val respHeaders = exchange.getResponseHeaders()
    val (input, output) = exchange.toRaw()
    val mainChannel = AsyncChannel.create(input, output)
    val connection = WebSocketConnectionImpl(
      _input = input,
      _output = output,
      masking = masking,
      bufferSize = bufferSize,
      mainChannel = mainChannel,
    )
    return SuccessWebSocketConnection(
      connection = connection,
      headers = respHeaders,
    )
  }
}
