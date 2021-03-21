package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.net.Path
import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncReader
import pw.binom.io.AsyncWriter
import pw.binom.io.http.Headers
import pw.binom.io.http.MutableHeaders
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.net.Query
import kotlin.js.JsName

interface HttpRequest : AsyncCloseable {
    val method: String
    val headers: Headers
    val path: Path
    val query:Query?
    val request:String
    fun readBinary(): AsyncInput
    fun readText(): AsyncReader
    suspend fun acceptWebsocket(): WebSocketConnection
    suspend fun rejectWebsocket()
    suspend fun response(): HttpResponse
    suspend fun <T> response(func: (HttpResponse)->T): T

    @JsName("HttpResponse2")
    val response: HttpResponse?
}

interface HttpResponse : AsyncCloseable {
    var status: Int
    val headers: MutableHeaders
    suspend fun writeBinary(): AsyncOutput
    suspend fun writeText(): AsyncWriter
}