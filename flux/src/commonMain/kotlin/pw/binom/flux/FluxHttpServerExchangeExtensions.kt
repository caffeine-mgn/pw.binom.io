package pw.binom.flux

import kotlinx.serialization.serializer
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders

suspend inline fun <reified T> FluxHttpServerExchange.response(
    value: T,
    status: Int = 200,
    headers: Headers = emptyHeaders(),
) = response(
    status = status,
    headers = headers,
    value = value,
    serializer = serializer(),
)
