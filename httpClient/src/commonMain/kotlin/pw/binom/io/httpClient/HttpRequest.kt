package pw.binom.io.httpClient

import pw.binom.io.*
import pw.binom.io.http.*
import pw.binom.url.URL

interface HttpRequest : AsyncCloseable {
    val headers: MutableHeaders
    val method: String
    val uri: URL
    var request: String

    /**
     * Starts write binary request
     * Closes this [DefaultHttpRequest] and delegate control to returned [AsyncHttpRequestOutput].
     */
    suspend fun writeData(): AsyncHttpRequestOutput
    suspend fun writeData(data: ByteBuffer) = writeData {
        it.writeFully(data)
    }

    suspend fun writeData(func: suspend (AsyncHttpRequestOutput) -> Unit): HttpResponse {
        val resp = writeData()
        func(resp)
        return resp.getResponse()
    }

    /**
     * Starts write text request
     * Closes this [DefaultHttpRequest] and delegate control to returned [AsyncHttpRequestWriter].
     */
    suspend fun writeText(): AsyncHttpRequestWriter
    suspend fun writeText(func: suspend (AsyncHttpRequestWriter) -> Unit): HttpResponse {
        val e = writeText()
        func(e)
        return e.getResponse()
    }

    /**
     * Starts to get HTTP response
     * Closes this [DefaultHttpRequest] and delegate control to returned [HttpResponse].
     */
    suspend fun getResponse(): HttpResponse
    suspend fun <T> useResponse(func: suspend (HttpResponse) -> T): T =
        getResponse().use { func(it) }
}

internal fun generateWebSocketHeaders(self: HttpRequest): Headers {
    val host = self.uri.host + (self.uri.port?.let { ":$it" } ?: "")
    return headersOf(
        Headers.ORIGIN to host,
        Headers.HOST to host,
    )
}

fun <T : HttpRequest> T.setHeader(key: String, value: String): T {
    headers[key] = value
    return this
}

fun <T : HttpRequest> T.addHeader(key: String, value: String): T {
    headers.add(key, value)
    return this
}

interface AsyncHttpRequestOutput : AsyncOutput {
    suspend fun getResponse(): HttpResponse
}

interface AsyncHttpRequestWriter : AsyncWriter {
    suspend fun getResponse(): HttpResponse
}

fun <T : HttpRequest> T.use(basicAuth: BasicAuth): T {
    headers.useBasicAuth(basicAuth)
    return this
}
