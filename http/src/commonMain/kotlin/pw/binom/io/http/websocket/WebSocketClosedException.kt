package pw.binom.io.http.websocket

import pw.binom.network.SocketClosedException

class WebSocketClosedException(val code: Short) : SocketClosedException() {
    override val message: String
        get() = "Code #$code"

    companion object {
        const val ABNORMALLY_CLOSE = 1006.toShort()
    }
}