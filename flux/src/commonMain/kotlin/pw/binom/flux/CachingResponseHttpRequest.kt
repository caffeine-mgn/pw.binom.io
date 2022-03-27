package pw.binom.flux

import pw.binom.AsyncInput
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.net.Path
import pw.binom.net.Query

class CachingResponseHttpRequest(val original: HttpRequest) : HttpRequest {
    override val headers: Headers
        get() = original.headers
    override val method: String
        get() = original.method
    override val path: Path
        get() = original.path
    override val query: Query?
        get() = original.query
    override val request: String
        get() = original.request

    private var resp: CachingHttpHttpResponse? = null

    override val response: CachingHttpHttpResponse?
        get() = resp

    override suspend fun acceptTcp(): AsyncChannel = original.acceptTcp()

    override suspend fun acceptWebsocket(masking: Boolean): WebSocketConnection = original.acceptWebsocket(masking)

    override suspend fun asyncClose() {
        original.asyncClose()
    }

    override fun readBinary(): AsyncInput =
        original.readBinary()

    override fun readText(): AsyncReader =
        original.readText()

    override suspend fun rejectTcp() = original.rejectTcp()

    override suspend fun rejectWebsocket() {
        original.rejectWebsocket()
    }

    override suspend fun response(): HttpResponse {
        if (resp != null) {
            throw IllegalStateException("Response already started")
        }
        val r = CachingHttpHttpResponse(original.response())
        resp = r
        return r
    }
}
