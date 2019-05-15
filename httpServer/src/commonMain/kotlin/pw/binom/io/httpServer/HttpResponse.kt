package pw.binom.io.httpServer

import pw.binom.io.AsyncOutputStream
import pw.binom.io.OutputStream
import pw.binom.io.socket.SocketChannel

interface HttpResponse {
    var status: Int
    val output: AsyncOutputStream
    fun resetHeader(name: String, value: String)
    val headers: Map<String, List<String>>
    fun addHeader(name: String, value: String)
    fun detach(): HttpConnectionState
    fun disconnect()
}