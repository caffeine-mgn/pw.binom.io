package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.io.http.EmptyHeaders
import pw.binom.io.http.Headers

@Deprecated(message = "Use HttpServer2")
interface FluxServerSerialization {
    suspend fun <T : Any> encode(
        request: FluxHttpRequest,
        value: T,
        serializer: KSerializer<T>,
        headers: Headers = EmptyHeaders,
        statusCode: Int? = null,
    )

    suspend fun <T : Any> decode(request: FluxHttpRequest, serializer: KSerializer<T>): T
}
