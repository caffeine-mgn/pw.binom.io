package pw.binom.io.socket

import pw.binom.Queue
import pw.binom.Stack
import pw.binom.io.Closeable

expect class SocketSelector(connections: Int) : Closeable {
    fun reg(channel: Channel, attachment: Any? = null): SelectorKey

    fun process(timeout: Int? = null, func: (SelectorKey) -> Unit): Boolean
    val keys: Collection<SelectorKey>

    interface SelectorKey {
        val channel: Channel
        val attachment: Any?
        fun cancel()
    }
}

/**
 * Returns [SocketSelector.SelectorKey] Queue
 */
fun SocketSelector.asQueue(timeout: Int? = 1): Queue<SocketSelector.SelectorKey> = object : Queue<SocketSelector.SelectorKey> {

    private val list = Stack<SocketSelector.SelectorKey>().asFiFoQueue()

    private fun checkFull() {
        if (list.isEmpty) {
            this@asQueue.process(timeout) {
                list.push(it)
            }
        }
    }

    override val isEmpty: Boolean
        get() {
            checkFull()
            return list.isEmpty
        }

    override fun pop(): SocketSelector.SelectorKey {
        checkFull()
        return list.pop()
    }

}