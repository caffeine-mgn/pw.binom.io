package pw.binom.io.httpServer

import pw.binom.io.AsyncChannel
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.HttpException

suspend fun HttpServerExchange.acceptTcp(): AsyncChannel {
  val upgrade = requestHeaders[Headers.UPGRADE]?.singleOrNull()
  if (upgrade == null) {
    println("All headers:\n$requestHeaders")
    throw HttpException(
      403,
      "Missing header \"${Headers.UPGRADE}\"",
    )
  }
  if (!upgrade.equals(Headers.TCP, true)) {
    throw HttpException(
      403,
      "Invalid Client Headers: Invalid Header \"${Headers.UPGRADE}\". Expected \"${Headers.TCP}\", Actual \"$upgrade\"",
    )
  }
  val headers = HashHeaders2()
  headers[Headers.CONNECTION] = Headers.UPGRADE
  headers[Headers.UPGRADE] = Headers.TCP
  startResponse(101, headers)
  return AsyncChannel.create(
    input = input,
    output = output,
  )
}
