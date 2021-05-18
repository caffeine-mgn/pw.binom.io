package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.popOrNull
import pw.binom.utils.TreeMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class DeadlineTimer(val errorProcessing: ((Throwable) -> Unit)? = null) : Closeable {
    private val lock = Lock()
    private val condition = lock.newCondition()
    private val worker = Worker()
    private val startTime = TimeSource.Monotonic.markNow()
    private val queue = ConcurrentQueue<Pair<Duration, () -> Unit>>()
    private val closedFlag = AtomicBoolean(false)

    init {
        doFreeze()
        worker.execute(Unit) {
            val tasks = TreeMap<Duration, ArrayList<() -> Unit>>()
            while (!closedFlag.value) {
                if (tasks.isEmpty() && queue.isEmpty) {
                    val e = queue.popBlocked()
                    tasks.getOrPut(e.first) { ArrayList() }.add(e.second)
                } else {
                    while (!queue.isEmpty) {
                        val e = queue.popOrNull() ?: break
                        tasks.getOrPut(e.first) { ArrayList() }.add(e.second)
                    }
                }
                val c = tasks.firstEntry ?: continue
                if (startTime.elapsedNow() >= c.key) {
                    tasks.remove(c.key)
                    c.value.forEach {
                        try {
                            it()
                        } catch (e: Throwable) {
                            errorProcessing?.invoke(e)
                        }
                    }
                }
                val signal =
                    lock.synchronize { condition.await(c.key - startTime.elapsedNow()) }
                if (closedFlag.value) {
                    return@execute
                }
                if (signal) {
                    continue
                    //wakeup by signal
                } else {
                    //wakeup by time
                    if (startTime.elapsedNow() < c.key) {
                        continue
                    }
                    tasks.remove(c.key)
                    c.value.forEach {
                        try {
                            it()
                        } catch (e: Throwable) {
                            errorProcessing?.invoke(e)
                        }
                    }
                }
            }

        }
    }

    /**
     * @param delay delay in milliseconds
     * @param func function for call after [delay]
     */
    fun delay(delay: Duration, func: () -> Unit) {
        lock.synchronize {
            val endTime = startTime.elapsedNow() + delay
            queue.push((endTime to func).doFreeze())
            condition.signal()
        }
    }

    suspend fun delay(delay: Duration) {
        suspendCoroutine<Unit> { con ->
            val dispatcher = con.getCrossThreadCoroutine() ?: return@suspendCoroutine
            dispatcher.doFreeze()
            val conRef = con.asReference()
            delay(delay) {
//                println("!   ${dispatcher}")
                dispatcher.coroutine(Result.success(Unit), conRef as Reference<Continuation<Any?>>)
//                println("2   ${dispatcher}")
            }
        }
    }

    override fun close() {
        if (closedFlag.value) {
            throw ClosedException()
        }
        closedFlag.value = true
        lock.synchronize {
            condition.signal()
        }
        worker.requestTermination().joinAndGetOrThrow()
    }
}