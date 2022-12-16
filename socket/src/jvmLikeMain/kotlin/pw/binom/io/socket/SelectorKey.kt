package pw.binom.io.socket

import pw.binom.io.Closeable
import java.nio.channels.SelectionKey

actual class SelectorKey(val native: SelectionKey, actual val selector: Selector) : Closeable {
    override fun close() {
        closed = true
        native.cancel()
    }

    actual var attachment: Any? = null
    private var closed = false
    actual var listenFlags: Int = 0
        set(value) {
            field = value
            if (closed) {
                return
            }
//            var r = if (value != 0) SelectionKey.OP_CONNECT else 0
            var r = 0
            if (value and KeyListenFlags.ERROR != 0 || value and KeyListenFlags.READ != 0) {
                r = r or SelectionKey.OP_READ or SelectionKey.OP_ACCEPT
            }

            if (value and KeyListenFlags.WRITE != 0) {
                r = r or SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT
            }
            try {
                native.interestOps(r and native.channel().validOps())
            } catch (e: java.nio.channels.CancelledKeyException) {
                closed = true
            }
        }
    actual val isClosed: Boolean
        get() = closed || !native.isValid

    override fun toString(): String = buildToString()
}
