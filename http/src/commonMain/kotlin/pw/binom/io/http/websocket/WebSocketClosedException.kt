package pw.binom.io.http.websocket

import pw.binom.network.ChannelClosedException

class WebSocketClosedException(val connection: WebSocketConnection) : ChannelClosedException() {

    companion object {
        const val CLOSE_NORMAL = 1000.toShort()
        const val ABNORMALLY_CLOSE = 1006.toShort()
    }
}
