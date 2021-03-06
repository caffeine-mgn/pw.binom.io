package pw.binom.io.httpClient

import pw.binom.AsyncOutput
import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncWriter
import pw.binom.io.http.BasicAuth
import pw.binom.io.http.MutableHeaders
import pw.binom.io.http.use
import pw.binom.io.http.websocket.WebSocketConnection

interface HttpRequest : AsyncCloseable {
    val headers: MutableHeaders

    /**
     * Starts write binary request
     * Closes this [DefaultHttpRequest] and delegate control to returned [AsyncHttpRequestOutput].
     */
    suspend fun writeData(): AsyncHttpRequestOutput

    /**
     * Starts write text request
     * Closes this [DefaultHttpRequest] and delegate control to returned [AsyncHttpRequestWriter].
     */
    suspend fun writeText(): AsyncHttpRequestWriter

    /**
     * Starts to get HTTP response
     * Closes this [DefaultHttpRequest] and delegate control to returned [HttpResponse].
     */
    suspend fun getResponse(): HttpResponse

    /**
     * Starts WebSocket Session.
     * Closes this [DefaultHttpRequest] and delegate control to returned [WebSocketConnection].
     */
    suspend fun startWebSocket(): WebSocketConnection
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
    headers.use(basicAuth)
    return this
}