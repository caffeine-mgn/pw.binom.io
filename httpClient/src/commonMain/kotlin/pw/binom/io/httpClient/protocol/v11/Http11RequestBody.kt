package pw.binom.io.httpClient.protocol.v11

import pw.binom.io.AsyncBufferedAsciiInputReader
import pw.binom.io.AsyncBufferedAsciiWriter
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.AbstractHttpRequestBody
import pw.binom.io.httpClient.HttpResponse

class Http11RequestBody(
    override val headers: Headers,
    override val autoFlushBuffer: Int,
    override val input: AsyncBufferedAsciiInputReader,
    override val output: AsyncBufferedAsciiWriter,
    private val requestFinishedListener: RequestFinishedListener? = null,
) : AbstractHttpRequestBody() {

    private var flushed = false

    override suspend fun flush(): HttpResponse {
        check(!flushed) { "Already flushed" }
        flushed = true
        val resp = Http11ConnectFactory2.readResponse(input)
        val resultInput = Http11ConnectFactory2.prepareHttpResponse(
            stream = input,
            contentLength = resp.headers.contentLength?.toLong(),
            contentEncoding = resp.headers.getContentEncodingList(),
            transferEncoding = resp.headers.getTransferEncodingList(),
        )

        return Http11Response(
            resp = resp,
            inputStream = resultInput,
            requestFinishedListener = requestFinishedListener,
            defaultKeepAlive = resp.version == Http11ConnectFactory2.Http1Version.V1_1,
        )
    }

    override suspend fun asyncClose() {
        if (flushed) {
            return
        }
        flushed = true
        requestFinishedListener?.requestFinished(
            responseKeepAlive = false,
            success = false,
        )
    }
}
