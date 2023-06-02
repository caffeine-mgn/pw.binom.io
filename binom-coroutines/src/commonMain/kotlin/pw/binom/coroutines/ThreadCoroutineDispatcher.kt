package pw.binom.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.synchronize
import pw.binom.io.Closeable
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext

class ThreadCoroutineDispatcher : CoroutineDispatcher(), Closeable {
    private val readyForWriteListener = BatchExchange<Runnable>()
    override fun isDispatchNeeded(context: CoroutineContext): Boolean = Thread.currentThread !== thread
    private val lock = ReentrantLock()
    private val lockCondition = lock.newCondition()
    private val closed = AtomicBoolean(false)

    private val thread = Thread { thread ->
        while (!closed.getValue()) {
            lock.synchronize {
                if (readyForWriteListener.isEmpty()) {
                    lockCondition.await()
                }
                readyForWriteListener.popAll {
                    it.forEach {
                        try {
                            it.run()
                        } catch (e: Throwable) {
                            thread.uncaughtExceptionHandler.uncaughtException(
                                thread = thread,
                                throwable = e,
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        thread.start()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        readyForWriteListener.push(block)
        lock.synchronize {
            lockCondition.signalAll()
        }
    }

    override fun close() {
        lock.synchronize {
            closed.setValue(true)
            lockCondition.signalAll()
        }
        thread.join()
    }
}
