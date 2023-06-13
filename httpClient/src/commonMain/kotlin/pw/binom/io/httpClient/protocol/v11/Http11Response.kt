package pw.binom.io.httpClient.protocol.v11

import pw.binom.io.AsyncInput
import pw.binom.io.http.AsyncHttpInput
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpResponse

class Http11Response(
    resp: Http11ConnectFactory2.Response,
    val inputStream: AsyncInput,
    private val requestFinishedListener: RequestFinishedListener? = null,
    private val defaultKeepAlive: Boolean,
) : HttpResponse {
    override val responseCode: Int = resp.responseCode
    override val headers: Headers = resp.headers

    override suspend fun readData(): AsyncInput = inputStream

    override suspend fun asyncClose() {
        requestFinishedListener?.requestFinished(
            success = inputStream is AsyncHttpInput && inputStream.isEof,
            responseKeepAlive = headers.keepAlive ?: defaultKeepAlive,
        )
    }
}
