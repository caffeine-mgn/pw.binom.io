package pw.binom.io.http.websocket

import pw.binom.io.IOException

class WebSocketClosedException(val code: Short) : IOException() {
    override val message: String
        get() = "Code #$code"

    companion object {
        const val ABNORMALLY_CLOSE = 1006.toShort()
    }
}