package pw.binom.flux.client

import kotlinx.serialization.KSerializer
import pw.binom.io.http.MutableHeaders
import pw.binom.io.httpClient.AsyncHttpRequestOutput
import pw.binom.io.httpClient.AsyncHttpRequestWriter
import pw.binom.io.httpClient.HttpRequest
import pw.binom.url.URL

internal class RestRequestImpl(
    val request1: HttpRequest,
    val serialization: RestClientSerialization,
    override var request: String = request1.request,
) : RestRequest {
    override suspend fun <T : Any> writeObject(serializer: KSerializer<T>, obj: T) {
        serialization.encode(
            request = this,
            value = obj,
            serializer = serializer,
        )
    }

    override val headers: MutableHeaders
        get() = request1.headers
    override val method: String
        get() = request1.method
    override val url: URL
        get() = request1.url

    override suspend fun asyncClose() {
        request1.asyncClose()
    }

    override suspend fun getResponse(): RestResponse =
        RestResponseImpl(resp = request1.getResponse(), serialization = serialization)

    override suspend fun writeBinary(): AsyncHttpRequestOutput = request1.writeBinary()

//    override suspend fun writeBinary(func: suspend (AsyncHttpRequestOutput) -> Unit): RestResponse =
//        RestResponseImpl(resp = request1.writeBinaryAndGetResponse(func), serialization = serialization)

    override suspend fun writeText(): AsyncHttpRequestWriter = request1.writeText()

//    override suspend fun<T> writeText(func: suspend (AsyncHttpRequestWriter) -> T): T =
//        RestResponseImpl(resp = request1.writeTextAndGetResponse(func), serialization = serialization)
}
