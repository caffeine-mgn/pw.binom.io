package pw.binom.io.httpServer

import pw.binom.io.OutputStream
import pw.binom.io.socket.SocketChannel

interface HttpResponse {
    var status: Int
    val output: OutputStream
    fun resetHeader(name: String, value: String)
    val headers: Map<String, List<String>>
    fun addHeader(name: String, value: String)
    fun detach(): SocketChannel
    fun disconnect()
}