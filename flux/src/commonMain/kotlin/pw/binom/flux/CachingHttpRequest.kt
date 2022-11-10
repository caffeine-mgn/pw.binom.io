package pw.binom.flux

import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.HttpRequest
import pw.binom.io.httpServer.HttpResponse
import pw.binom.io.httpServer.StubHttpResponse
import pw.binom.net.Path
import pw.binom.net.Query
import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool

class CachingHttpRequest(
    val responsePool: ObjectPool<CachingHttpResponse>,
    val onClose: (CachingHttpRequest) -> Unit
) : HttpRequest {
    companion object {
        fun factory(responsePool: ObjectPool<CachingHttpResponse>) = object : ObjectFactory<CachingHttpRequest> {
            override fun allocate(pool: ObjectPool<CachingHttpRequest>): CachingHttpRequest {
                WebMetrics.cachingHttpRequest.inc()
                return CachingHttpRequest(responsePool = responsePool) {
                    pool.recycle(it)
                }
            }

            override fun deallocate(value: CachingHttpRequest, pool: ObjectPool<CachingHttpRequest>) {
                WebMetrics.cachingHttpRequest.dec()
            }
        }
    }

    var original: HttpRequest? = null
        private set

    suspend fun reset(original: HttpRequest) {
        this.original = original
        resp = null
        needClose = true
    }

    private var needClose = true
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

    private var resp: HttpResponse? = null

    override val response: HttpResponse?
        get() = resp
    override val isReadyForResponse: Boolean
        get() = original!!.isReadyForResponse

    override suspend fun acceptTcp(): AsyncChannel {
        val r = original!!.acceptTcp()
        needClose = false
        resp = StubHttpResponse
        return r
    }

    override suspend fun acceptWebsocket(masking: Boolean): WebSocketConnection {
        val r = original!!.acceptWebsocket(masking)
        resp = StubHttpResponse
        needClose = false
        return r
    }

    override suspend fun asyncClose() {
    }

    override fun readBinary(): AsyncInput = original!!.readBinary()

    override fun readText(): AsyncReader = original!!.readText()

    override suspend fun rejectTcp() = original!!.rejectTcp()

    override suspend fun rejectWebsocket() {
        original!!.rejectWebsocket()

        needClose = false
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
                if (resp is CachingHttpResponse) {
                    resp.finish()
                }
            }
        } finally {
            original = null
            resp = null
            onClose(this)
        }
    }
}
