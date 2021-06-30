package pw.binom.flux

import kotlinx.serialization.KSerializer
import pw.binom.AsyncOutput
import pw.binom.io.http.EmptyHeaders
import pw.binom.io.http.Headers

interface Serialization {
    suspend fun <T : Any> encode(
        request: FluxHttpRequest,
        value: T, serializer: KSerializer<T>,
        headers: Headers = EmptyHeaders,
        statusCode: Int? = null
    )

    suspend fun <T : Any> decode(request: FluxHttpRequest, serializer: KSerializer<T>): T
}