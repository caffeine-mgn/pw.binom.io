package pw.binom.flux

import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.net.Path
import pw.binom.net.Query
import pw.binom.pool.DefaultPool

class CachingHttpRequest(
    val responsePool: DefaultPool<CachingHttpResponse>,
    val onClose: (CachingHttpRequest) -> Unit
) : HttpRequest {
    var original: HttpRequest? = null
        private set

    suspend fun reset(original: HttpRequest) {
        this.original = original
        resp = null
    }

    override val headers: Headers
        get() = original!!.headers
    override val method: String
        get() = original!!.method
    override val path: Path
        get() = original!!.path
    override val query: Query?
        get() = original!!.query
    override val request: String
        get() = original!!.request

    private var resp: CachingHttpResponse? = null

    override val response: CachingHttpResponse?
        get() = resp

    override suspend fun acceptTcp(): AsyncChannel = original!!.acceptTcp()

    override suspend fun acceptWebsocket(masking: Boolean): WebSocketConnection = original!!.acceptWebsocket(masking)

    override suspend fun asyncClose() {
    }

    override fun readBinary(): AsyncInput =
        original!!.readBinary()

    override fun readText(): AsyncReader =
        original!!.readText()

    override suspend fun rejectTcp() = original!!.rejectTcp()

    override suspend fun rejectWebsocket() {
        original!!.rejectWebsocket()
    }

    override suspend fun response(): HttpResponse {
        if (resp != null) {
            throw IllegalStateException("Response already started")
        }
        val response = responsePool.borrow()
        response.resetOriginal(original!!.response(), this)
        resp = response
        return response
    }

    internal suspend fun finish() {
        try {
            val resp = resp
            if (resp == null) {
                original?.asyncClose()
            } else {
                resp.finish()
            }
        } finally {
            original = null
            resp = null
            onClose(this)
        }
    }
}
