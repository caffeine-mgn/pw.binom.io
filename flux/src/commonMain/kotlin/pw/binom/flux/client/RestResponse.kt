package pw.binom.flux.client

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import pw.binom.io.httpClient.HttpResponse

interface RestResponse : HttpResponse {
    suspend fun <T : Any> readObject(serializer: KSerializer<T>): T
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> RestResponse.readObject() {
    readObject(T::class.serializer())
}
