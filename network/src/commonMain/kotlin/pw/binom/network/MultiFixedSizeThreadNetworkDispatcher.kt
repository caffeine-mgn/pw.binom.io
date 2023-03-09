package pw.binom.network

import kotlinx.coroutines.Runnable
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.io.socket.Selector
import pw.binom.thread.FixedThreadExecutorService
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext

class MultiFixedSizeThreadNetworkDispatcher(threadSize: Int) : AbstractNetworkManager(), Closeable {
    override val selector = Selector()

    val taskCount: Int
        get() = threads.taskCount

    init {
        require(threadSize > 0) { "threadSize should be more than 0" }
    }

    private val threads = FixedThreadExecutorService(threadSize)
    private val selectThread = Thread {
        SelectExecutor.startSelecting(
            selector = selector,
            isSelectorClosed = { closed.getValue() },
            submitTask = threads::submit,
        )
    }

    private val closed = AtomicBoolean(false)
    override fun ensureOpen() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        ensureOpen()
        return !threads.isThreadFromPool(Thread.currentThread)
    }

//    override val key: CoroutineContext.Key<*>
//        get() = NetworkInterceptor.Key

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        ensureOpen()
        threads.submit { block.run() }
        selector.wakeup()
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            return
        }
        println("MultiFixedSizeThreadNetworkDispatcher::close #1")
        threads.shutdownNow()
        println("MultiFixedSizeThreadNetworkDispatcher::close #2")
        selector.wakeup()
        println("MultiFixedSizeThreadNetworkDispatcher::close #3")
        selectThread.join()
        println("MultiFixedSizeThreadNetworkDispatcher::close #4")
        selector.close()
        println("MultiFixedSizeThreadNetworkDispatcher::close #5")
    }

    init {
        selectThread.start()
    }
}
