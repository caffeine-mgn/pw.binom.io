package pw.binom.flux.client

import kotlinx.serialization.KSerializer
import pw.binom.io.AsyncChannel
import pw.binom.io.http.MutableHeaders
import pw.binom.io.http.websocket.WebSocketConnection
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
    override val uri: URL
        get() = request1.uri

    override suspend fun asyncClose() {
        request1.asyncClose()
    }

    override suspend fun getResponse(): RestResponse =
        RestResponseImpl(resp = request1.getResponse(), serialization = serialization)

    override suspend fun startTcp(): AsyncChannel = request1.startTcp()

    override suspend fun startWebSocket(origin: String?, masking: Boolean): WebSocketConnection =
        request1.startWebSocket(origin = origin, masking = masking)

    override suspend fun writeData(): AsyncHttpRequestOutput = request1.writeData()

    override suspend fun writeData(func: suspend (AsyncHttpRequestOutput) -> Unit): RestResponse =
        RestResponseImpl(resp = request1.writeData(func), serialization = serialization)

    override suspend fun writeText(): AsyncHttpRequestWriter = request1.writeText()

    override suspend fun writeText(func: suspend (AsyncHttpRequestWriter) -> Unit): RestResponse =
        RestResponseImpl(resp = request1.writeText(func), serialization = serialization)
}
