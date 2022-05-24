package pw.binom.io.http.websocket

import pw.binom.network.SocketClosedException

class WebSocketClosedException(val connection: WebSocketConnection, val code: Short) : SocketClosedException() {
    override val message: String
        get() = "Code #$code"

    companion object {
        const val CLOSE_NORMAL = 1000.toShort()
        const val ABNORMALLY_CLOSE = 1006.toShort()
    }
}
