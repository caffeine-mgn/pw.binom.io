package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncReader
import pw.binom.io.AsyncWriter
import pw.binom.io.http.Headers
import pw.binom.io.http.MutableHeaders
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.use
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
    suspend fun acceptWebsocket(): WebSocketConnection
    suspend fun rejectWebsocket()
    suspend fun response(): HttpResponse
    suspend fun <T> response(func: (HttpResponse) -> T): T

    @JsName("HttpResponse2")
    val response: HttpResponse?
}

interface HttpResponse : AsyncCloseable {
    var status: Int
    val headers: MutableHeaders
    fun setContentType(value: String): HttpResponse {
        headers.contentType = value
        return this
    }

    fun setStatus(status: Int): HttpResponse {
        this.status = status
        return this
    }

    suspend fun writeBinary(): AsyncOutput
    suspend fun writeBinary(data: ByteBuffer) {
        writeBinary().use {
            it.write(data)
        }
    }

    suspend fun writeBinary(data: ByteArray) {
        data.wrap {
            writeBinary(it)
        }
    }

    suspend fun writeText(): AsyncWriter
    suspend fun writeText(text: String) {
        writeText().use {
            it.append(text)
        }
    }
}