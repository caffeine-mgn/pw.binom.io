package pw.binom.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import pw.binom.BatchExchange
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.synchronize
import pw.binom.io.Closeable
import pw.binom.thread.Thread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class ThreadWorker : CoroutineDispatcher(), Closeable {

    private val readyForWriteListener = BatchExchange<Runnable>()
    override fun isDispatchNeeded(context: CoroutineContext): Boolean = Thread.currentThread !== thread
    private val r = ReentrantLock()
    private val c = r.newCondition()
    private val closed = AtomicBoolean(false)
    protected open fun beforeStart() {
        // Do nothing
    }

    protected open fun beforeStop() {
        // Do nothing
    }

    private val thread = Thread { thread ->
        beforeStart()
        try {
            while (!closed.getValue()) {
                r.synchronize {
                    if (readyForWriteListener.isEmpty()) {
                        c.await()
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
        } finally {
            beforeStop()
        }
    }

    init {
        thread.start()
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        readyForWriteListener.push(block)
        r.synchronize {
            c.signalAll()
        }
    }

    fun dispatch(context: CoroutineContext = EmptyCoroutineContext, block: () -> Unit) {
        dispatch(context, Runnable(block))
    }

//    fun <T> execute(func: () -> T): T {
//        var result: T? = null
//        val lock = ReentrantLock()
//        val con = lock.newCondition()
//        r.synchronize {
//            readyForWriteListener.push(
//                Runnable {
//                    lock.synchronize {
//                        result = func()
//                        con.signalAll()
//                    }
//                }
//            )
//            c.await()
//        }
//        return result as T
//    }

    override fun close() {
        r.synchronize {
            closed.setValue(true)
            c.signalAll()
        }
        thread.join()
    }
}
