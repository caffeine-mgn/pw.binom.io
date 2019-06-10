package pw.binom.io.httpServer

import pw.binom.doFreeze
import pw.binom.io.socket.RawSocketChannel
import pw.binom.io.socket.SocketChannel

class HttpConnectionState(
        val status: Int,
        val responseHeaders: Map<String, List<String>>,
        val channel: SocketChannel,
        val headerSendded: Boolean,
        val requestHeaders: Map<String, List<String>>,
        val method: String,
        val uri: String) {
    init {
        doFreeze()
    }
}