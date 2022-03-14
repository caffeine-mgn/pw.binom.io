package pw.binom.flux.client

import kotlinx.serialization.KSerializer
import pw.binom.io.AsyncChannel
import pw.binom.io.http.MutableHeaders
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpClient.AsyncHttpRequestOutput
import pw.binom.io.httpClient.AsyncHttpRequestWriter
import pw.binom.io.httpClient.HttpRequest
import pw.binom.net.URL

internal class RestRequestImpl(val request: HttpRequest, val serialization: RestClientSerialization) : RestRequest {
    override suspend fun <T : Any> writeObject(serializer: KSerializer<T>, obj: T) {
        serialization.encode(
            request = this,
            value = obj,
            serializer = serializer,
        )
    }

    override val headers: MutableHeaders
        get() = request.headers
    override val method: String
        get() = request.method
    override val uri: URL
        get() = request.uri

    override suspend fun asyncClose() {
        request.asyncClose()
    }

    override suspend fun getResponse(): RestResponse =
        RestResponseImpl(resp = request.getResponse(), serialization = serialization)

    override suspend fun startTcp(): AsyncChannel = request.startTcp()

    override suspend fun startWebSocket(origin: String?, masking: Boolean): WebSocketConnection =
        request.startWebSocket(origin = origin, masking = masking)

    override suspend fun writeData(): AsyncHttpRequestOutput = request.writeData()

    override suspend fun writeData(func: suspend (AsyncHttpRequestOutput) -> Unit): RestResponse =
        RestResponseImpl(resp = request.writeData(func), serialization = serialization)

    override suspend fun writeText(): AsyncHttpRequestWriter = request.writeText()

    override suspend fun writeText(func: suspend (AsyncHttpRequestWriter) -> Unit): RestResponse =
        RestResponseImpl(resp = request.writeText(func), serialization = serialization)
}
