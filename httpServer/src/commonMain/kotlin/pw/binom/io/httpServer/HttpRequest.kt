package pw.binom.io.httpServer

import pw.binom.AsyncInput

interface HttpRequest {
    val method: String
    val uri: String
    val contextUri: String
    val input: AsyncInput
    val headers: Map<String, List<String>>
}

fun HttpRequest.withContextURI(uri:String)=object :HttpRequest{
    override val method: String
        get() = this@withContextURI.method
    override val uri: String
        get() = this@withContextURI.uri
    override val contextUri: String
        get() = uri
    override val input: AsyncInput
        get() = this@withContextURI.input
    override val headers: Map<String, List<String>>
        get() = this@withContextURI.headers
}