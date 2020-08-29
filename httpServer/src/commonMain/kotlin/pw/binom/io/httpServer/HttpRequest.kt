package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.io.socket.nio.SocketNIOManager

interface HttpRequest {
    val method: String
    val uri: String
    val contextUri: String
    val input: AsyncInput
    val rawInput: AsyncInput
    val rawOutput: AsyncOutput
    val rawConnection: SocketNIOManager.ConnectionRaw
    val headers: Map<String, List<String>>
}

fun HttpRequest.withContextURI(uri: String) = object : HttpRequest {
    override val method: String
        get() = this@withContextURI.method
    override val uri: String
        get() = this@withContextURI.uri
    override val contextUri: String
        get() = uri
    override val input: AsyncInput
        get() = this@withContextURI.input
    override val rawInput: AsyncInput
        get() = this@withContextURI.rawInput
    override val rawOutput: AsyncOutput
        get() = this@withContextURI.rawOutput
    override val rawConnection: SocketNIOManager.ConnectionRaw
        get() = this@withContextURI.rawConnection
    override val headers: Map<String, List<String>>
        get() = this@withContextURI.headers
}