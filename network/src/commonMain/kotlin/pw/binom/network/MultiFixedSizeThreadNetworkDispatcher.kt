package pw.binom.network

import kotlinx.coroutines.Runnable
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableList
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.io.socket.Selector
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext

class MultiFixedSizeThreadNetworkDispatcher(threadSize: Int) : AbstractNetworkManager(), Closeable {
    override val selector = Selector()

    private val exchange = BatchExchange<Runnable>()
//    private val exchange = Exchange<Runnable>()

    init {
        require(threadSize > 0) { "threadSize should be more than 0" }
    }

    private val closed = AtomicBoolean(false)
    override fun ensureOpen() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

    private val threads = defaultMutableList<NetworkThread>(threadSize)

    init {
        repeat(threadSize) {
            val thread = NetworkThread(
                selector = selector,
                exchange = exchange,
                name = "NetworkDispatcher-$it"
            )
            thread.start()
            threads += thread
        }
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        ensureOpen()
        val currentId = Thread.currentThread.id
        return !threads.any { it.id == currentId }
    }

//    override val key: CoroutineContext.Key<*>
//        get() = NetworkInterceptor.Key

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        ensureOpen()
        exchange.push(block)
        selector.wakeup()
    }

    override fun close() {
        if (closed.getValue()) {
            return
        }
        threads.forEach {
            it.close()
        }
        while (threads.isNotEmpty()) {
            threads.removeIf { !it.isActive }
            selector.wakeup()
            Thread.sleep(10)
        }
        selector.close()
    }
}
