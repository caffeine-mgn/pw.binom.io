package pw.binom.io.httpServer

import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.io.http.MutableHeaders
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.net.Path
import pw.binom.net.Query
import pw.binom.wrap
import kotlin.js.JsName

interface HttpRequest : AsyncCloseable {
    val method: String
    val headers: Headers
    val path: Path
    val query: Query?
    val request: String
    fun readBinary(): AsyncInput
    fun readText(): AsyncReader

    /**
     * Allow upgrade this connection to web-socket connection
     */
    suspend fun acceptWebsocket(masking: Boolean = false): WebSocketConnection

    /**
     * Reject upgrade this connection to web-socket connection
     */
    suspend fun rejectWebsocket()

    /**
     * Allow upgrade this connection to tcp connection
     */
    suspend fun acceptTcp(): AsyncChannel

    /**
     * Reject upgrade this connection to tcp connection
     */
    suspend fun rejectTcp()
    suspend fun response(): HttpResponse
    suspend fun <T> response(func: suspend (HttpResponse) -> T): T =
        response().use {
            func(it)
        }

    @JsName("HttpResponse2")
    val response: HttpResponse?
    val isReadyForResponse: Boolean
}

interface HttpResponse : AsyncCloseable {
    var status: Int
    val headers: MutableHeaders
    fun contentType(value: String): HttpResponse {
        headers.contentType = value
        return this
    }

    fun status(status: Int): HttpResponse {
        this.status = status
        return this
    }

    suspend fun startWriteBinary(): AsyncOutput
    suspend fun sendBinary(data: ByteBuffer) {
        startWriteBinary().use {
            it.write(data)
            it.flush()
        }
    }

    suspend fun <T> sendBinary(func: suspend (AsyncOutput) -> T): T =
        startWriteBinary().use {
            val result = func(it)
            it.flush()
            result
        }

    suspend fun sendBinary(data: ByteArray) {
        data.wrap {
            sendBinary(it)
        }
    }

    suspend fun <T> sendText(func: suspend (AsyncWriter) -> T): T =
        startWriteText().use {
            val result = func(it)
            it.flush()
            result
        }

    suspend fun startWriteText(): AsyncWriter
    suspend fun sendText(text: String) {
        startWriteText().use {
            it.append(text)
            it.flush()
        }
    }
}
