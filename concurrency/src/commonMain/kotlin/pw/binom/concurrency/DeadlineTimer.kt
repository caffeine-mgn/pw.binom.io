package pw.binom.concurrency

import pw.binom.TimeoutException
import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.popOrNull
import pw.binom.utils.TreeMap
import kotlin.coroutines.*
import kotlin.native.concurrent.SharedImmutable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@SharedImmutable
private val CLOSE_MARKER: () -> Unit = {}.doFreeze()

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
            MAIN_WORKER_LOOP@ while (!closedFlag.value) {
                if (tasks.isEmpty() && queue.isEmpty) {
                    val e = queue.popBlocked()
                    if (e.second === CLOSE_MARKER) {
                        break
                    }
                    tasks.getOrPut(e.first) { ArrayList() }.add(e.second)
                } else {
                    while (!queue.isEmpty) {
                        val e = queue.popOrNull() ?: break
                        if (e.second === CLOSE_MARKER) {
                            break@MAIN_WORKER_LOOP
                        }
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
                    continue
                }
                val signal =
                    lock.synchronize { condition.await(c.key - startTime.elapsedNow()) }
                if (closedFlag.value) {
                    break@MAIN_WORKER_LOOP
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
            tasks.clear()

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

    /**
     * Starts [func] in current thread. If [func] can't execute in [delay] duration will throw [TimeoutException]
     */
    suspend fun <T> timeout(delay: Duration, func: suspend () -> T): T =
        suspendCoroutine { con ->
            val dispatcher = con.getCrossThreadCoroutine() ?: return@suspendCoroutine
            val functionDone = AtomicBoolean(false)
            func.startCoroutine(object : Continuation<T> {
                override val context: CoroutineContext = con.context

                override fun resumeWith(result: Result<T>) {
                    if (functionDone.compareAndSet(false, true)) {
                        con.resumeWith(result = result)
                    }
                }
            })
            val conRef = con.asReference()
            delay(delay) {
                if (functionDone.compareAndSet(false, true)) {
                    dispatcher.coroutine(Result.failure(TimeoutException()), conRef as Reference<Continuation<Any?>>)
                } else {
                    conRef.close()
                }
            }
        }

    @Suppress("UNCHECKED_CAST")
    suspend fun delay(delay: Duration) {
        suspendCoroutine<Unit> { con ->
            val dispatcher = con.getCrossThreadCoroutine() ?: return@suspendCoroutine
            dispatcher.doFreeze()
            val conRef = con.asReference()
            delay(delay) {
                dispatcher.coroutine(Result.success(Unit), conRef as Reference<Continuation<Any?>>)
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
        queue.push((Duration.seconds(123) to CLOSE_MARKER).doFreeze())
        worker.requestTermination().joinAndGetOrThrow()
    }
}

