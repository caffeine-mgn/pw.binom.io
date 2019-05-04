package pw.binom.io.socket

import pw.binom.io.Closeable

expect class SocketSelector(connections: Int) : Closeable {
    fun reg(channel: Channel, attachment: Any? = null): SelectorKey

    fun process(timeout: Int? = null, func: (SelectorKey) -> Unit): Boolean
    val keys:Collection<SelectorKey>

    interface SelectorKey {
        val channel: Channel
        val attachment: Any?
        fun cancel()
    }
}