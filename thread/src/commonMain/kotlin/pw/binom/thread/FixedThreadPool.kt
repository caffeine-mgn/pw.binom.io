package pw.binom.thread

import pw.binom.AppendableQueue
import pw.binom.PopResult
import pw.binom.Stack
import pw.binom.io.Closeable
import pw.binom.printStacktrace
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class WaitEventQueue<T : Any>(val loadFactor: Float = 0.75f, val compactFactor: Float = 0.5f) : Closeable {
    private val ss = SynchronizedAppendableQueue(Stack<T>().asFiFoQueue())

    private val lock = Lock()
    private val condition = lock.newCondition()

    @OptIn(ExperimentalTime::class)
    fun pop(timeout: Duration): T? {
        lock.synchronize {
            if (ss.isEmpty) {
                if (!condition.wait(timeout))
                    return null
            }
            return ss.pop()
        }
    }

    fun push(value: T) {
        lock.synchronize {
            val n = ss.isEmpty
            ss.push(value)
            if (n)
                condition.notify()
        }
    }

    override fun close() {
        condition.close()
        lock.close()
    }
}

class FixedThreadPool(size: Int) : ThreadPool {
    val q = SynchronizedAppendableQueue(Stack<Item>().asFiFoQueue())//WaitEventQueue<Item>()
    private val threads = Array(size) { ExecuteThread() }

//    val statisticThread = Thread(Runnable {
//        while (!Thread.currentThread.isInterrupted) {
//            println("Thread count: ${threads.size}, jobs: ${q.size}")
//            Thread.sleep(1000)
//        }
//    }).also {
//        it.start()
//    }

    class Item(val func: () -> Any?, val continuation: Continuation<Any?>?)

    init {
        threads.forEach {
            it.start()
        }
    }

    private inner class ExecuteThread : Thread() {
        @OptIn(ExperimentalTime::class)
        val timeout = 1000.toDuration(DurationUnit.MILLISECONDS)
        override fun run() {
            while (!isInterrupted) {
                try {
                    val value = q.pop(timeout)
                            ?: if (shutdown)
                                break
                            else
                                continue
                    val continuation = value.continuation
                    if (continuation != null) {
                        continuation.resumeWith(kotlin.runCatching { value.func() })
                    } else {
                        value.func()
                    }

                } catch (e: InterruptedException) {
                    println("Interopted!")
                    break
                } catch (e: Throwable) {
                    println("Error!")
                    e.printStacktrace()
                }
            }
        }
    }

    override suspend fun <T> executeAsync(func: () -> T): T {
        if (shutdown)
            throw RuntimeException("Thread Pool in Shutdown state")

        return suspendCoroutine {
            q.push(Item(func, it as Continuation<Any?>))
        }
    }

    override fun execute(func: () -> Unit) {
        if (shutdown)
            throw RuntimeException("Thread Pool in Shutdown state")
        q.push(Item(func, null))
    }

    override fun <T> resume(continuation: Continuation<T>, result: Result<T>) {
        if (shutdown)
            throw RuntimeException("Thread Pool in Shutdown state")
        q.push(Item({
            continuation.resumeWith(result)
        }, null))
    }

    private var shutdown = false

    override fun shutdown() {
        shutdown = true
        threads.forEach {
            it.join()
        }
    }

    override fun close() {
//        statisticThread.interrupt()
        shutdown = true
        threads.forEach {
            it.interrupt()
        }
        threads.forEach {
            it.join()
        }
    }

}