package pw.binom.io.socket

import pw.binom.PopResult
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
        val isReadable: Boolean
        val isWritable: Boolean
        val listenReadable: Boolean
        val listenWritable: Boolean
        val isCanlelled: Boolean
        fun cancel()
        fun updateListening(read: Boolean, write: Boolean)
    }
}

/**
 * Returns [SocketSelector.SelectorKey] Queue
 */
fun SocketSelector.asQueue(timeout: Int? = 1): Queue<SocketSelector.SelectorKey> = object : Queue<SocketSelector.SelectorKey> {
    override val size: Int
        get() {
            checkFull()
            return list.size
        }

    override fun pop(dist: PopResult<SocketSelector.SelectorKey>) {
        if (isEmpty) {
            dist.clear()
        } else {
            dist.set(pop())
        }
    }

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