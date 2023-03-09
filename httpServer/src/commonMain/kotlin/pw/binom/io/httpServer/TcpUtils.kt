package pw.binom.io.httpServer

import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.HttpException

suspend fun HttpServerExchange.acceptTcp(): Pair<AsyncInput, AsyncOutput> {
    if (!requestHeaders[Headers.UPGRADE]?.singleOrNull().equals(Headers.TCP, true)) {
        throw HttpException(403, "Invalid Client Headers: Invalid Header \"${Headers.TCP}\"")
    }
    val headers = HashHeaders2()
    headers[Headers.CONNECTION] = Headers.UPGRADE
    headers[Headers.UPGRADE] = Headers.TCP
    startResponse(101, headers)
    return input to output
}
