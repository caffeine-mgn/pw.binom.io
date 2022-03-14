package pw.binom.flux.client

import kotlinx.serialization.KSerializer
import pw.binom.io.httpClient.HttpRequest
import pw.binom.io.httpClient.HttpResponse

interface RestClientSerialization {
    suspend fun <T : Any> encode(
        request: HttpRequest,
        value: T,
        serializer: KSerializer<T>,
    )

    suspend fun <T : Any> decode(request: HttpResponse, serializer: KSerializer<T>): T
}
