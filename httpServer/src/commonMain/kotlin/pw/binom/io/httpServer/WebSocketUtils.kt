package pw.binom.io.httpServer

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.crypto.Sha1MessageDigest
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.HttpException
import pw.binom.io.http.emptyHeaders
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.http.websocket.WebSocketConnectionImpl3

suspend fun HttpServerExchange.acceptWebsocket(
  masking: Boolean = false,
  headers: Headers = emptyHeaders(),
  bufferSize: Int = DEFAULT_BUFFER_SIZE,
): WebSocketConnection {
  val upgrade = requestHeaders[Headers.UPGRADE]?.lastOrNull()
  if (!upgrade.equals(Headers.WEBSOCKET, true)) {
    throw HttpException(
      code = 403,
      message = "Invalid Client Headers: Invalid Header \"${Headers.UPGRADE}\". Expected \"${Headers.WEBSOCKET}\", Actual \"$upgrade\"",
    )
  }
  val key = requestHeaders.getSingleOrNull(Headers.SEC_WEBSOCKET_KEY)
    ?: throw HttpException(403, "Invalid Client Headers: Missing Header \"${Headers.SEC_WEBSOCKET_KEY}\"")
  val sha1 = Sha1MessageDigest()
  val responseHeader = HashHeaders2()
  responseHeader[Headers.CONNECTION] = Headers.UPGRADE
  responseHeader[Headers.UPGRADE] = Headers.WEBSOCKET
  responseHeader[Headers.SEC_WEBSOCKET_ACCEPT] = HandshakeSecret.generateResponse(sha1, key)
  responseHeader.add(headers)
  startResponse(statusCode = 101, headers = responseHeader)
  return WebSocketConnectionImpl3(
    _output = output,
    _input = input,
    masking = masking,
    bufferSize = bufferSize,
    mainChannel = mainChannel,
  )
}
