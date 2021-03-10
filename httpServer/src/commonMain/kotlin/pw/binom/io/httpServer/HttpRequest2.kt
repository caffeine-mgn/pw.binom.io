package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.URN
import pw.binom.io.AsyncAppendable
import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncReader
import pw.binom.io.AsyncWriter
import pw.binom.io.http.Headers
import pw.binom.io.http.MutableHeaders
import pw.binom.io.http.websocket.WebSocketConnection

interface HttpRequest2 : AsyncCloseable {
    val method: String
    val headers: Headers
    val urn: URN
    fun readBinary(): AsyncInput
    fun readText(): AsyncReader
    suspend fun acceptWebsocket(): WebSocketConnection
    suspend fun rejectWebsocket()
    suspend fun response(): HttpResponse2
}

interface HttpResponse2 : AsyncCloseable {
    var status: Int
    val headers: MutableHeaders
    suspend fun writeBinary(): AsyncOutput
    suspend fun writeText(): AsyncWriter
}