package pw.binom.flux.client

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import pw.binom.io.httpClient.AsyncHttpRequestOutput
import pw.binom.io.httpClient.AsyncHttpRequestWriter
import pw.binom.io.httpClient.HttpRequest

interface RestRequest : HttpRequest {
    suspend fun <T : Any> writeObject(serializer: KSerializer<T>, obj: T)
    override suspend fun getResponse(): RestResponse
    override suspend fun writeText(func: suspend (AsyncHttpRequestWriter) -> Unit): RestResponse
    override suspend fun writeData(func: suspend (AsyncHttpRequestOutput) -> Unit): RestResponse
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T : Any> RestRequest.writeObject(value: T) {
    writeObject(T::class.serializer(), value)
}
