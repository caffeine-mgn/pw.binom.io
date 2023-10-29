package pw.binom.io.httpClient.protocol.v11

import pw.binom.io.AsyncInput
import pw.binom.io.http.AsyncHttpInput
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpResponse
import pw.binom.url.Path
import pw.binom.url.Query
import pw.binom.url.URL

class Http11Response(
    val url: URL,
    resp: Http11ConnectFactory2.Response,
    val inputStream: AsyncInput,
    private val requestFinishedListener: RequestFinishedListener? = null,
    private val defaultKeepAlive: Boolean,
) : HttpResponse {
    override val responseCode: Int = resp.responseCode
    override val inputHeaders: Headers = resp.headers
    override val path: Path
        get() = url.path
    override val query: Query?
        get() = url.query

    override suspend fun readBinary(): AsyncInput = inputStream

    override suspend fun asyncClose() {
        requestFinishedListener?.requestFinished(
            success = inputStream is AsyncHttpInput && inputStream.isEof,
            responseKeepAlive = inputHeaders.keepAlive ?: false,
        )
    }
}
