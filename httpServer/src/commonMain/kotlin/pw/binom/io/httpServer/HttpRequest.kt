package pw.binom.io.httpServer

import pw.binom.io.AsyncInputStream

interface HttpRequest {
    val method: String
    val uri: String
    val input: AsyncInputStream
    val headers: Map<String, List<String>>
}