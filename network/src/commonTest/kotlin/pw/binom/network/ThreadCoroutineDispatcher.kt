package pw.binom.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.Exchange
import pw.binom.io.Closeable
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext

class ThreadCoroutineDispatcher : CoroutineDispatcher(), Closeable {
    private val e = Exchange<Runnable>()
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        e.put(block)
    }

    private val closedFlag = AtomicBoolean(false)

    private val thread = Thread {
        while (!closedFlag.getValue()) {
            e.get().run()
        }
    }

    init {
        thread.start()
    }

    override fun close() {
        if (!closedFlag.compareAndSet(false, true)) {
            return
        }
        e.put(Runnable {})
        thread.join()
    }
}
