package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.network.TcpConnection

interface HttpRequest {
    val method: String
    val uri: String
    val contextUri: String
    val input: AsyncInput
    val rawInput: AsyncInput
    val rawOutput: AsyncOutput
    val rawConnection: TcpConnection
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
    override val rawConnection: TcpConnection
        get() = this@withContextURI.rawConnection
    override val headers: Map<String, List<String>>
        get() = this@withContextURI.headers
}