package pw.binom.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.TreeMap
import pw.binom.collections.defaultArrayList
import pw.binom.doFreeze
import pw.binom.io.ClosedException
import pw.binom.popOrNull
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.resumeWithException
import kotlin.native.concurrent.SharedImmutable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@SharedImmutable
private val CLOSE_MARKER: () -> Unit = {}.doFreeze()

@Deprecated(message = "Not use it. Will be deleted")
@OptIn(ExperimentalTime::class)
class DeadlineTimerImpl(val errorProcessing: ((Throwable) -> Unit)? = null) : DeadlineTimer {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val worker = Worker()
    private val startTime = TimeSource.Monotonic.markNow()
    private val queue = ConcurrentQueue<Pair<Duration, () -> Unit>>()
    private val closedFlag = AtomicBoolean(false)

    init {
        doFreeze()
        worker.execute(Unit) {
            val tasks = TreeMap<Duration, MutableList<() -> Unit>>()
            MAIN_WORKER_LOOP@ while (!closedFlag.getValue()) {
                if (tasks.isEmpty() && queue.isEmpty) {
                    val e = queue.popBlocked()
                    if (e.second === CLOSE_MARKER) {
                        break
                    }
                    tasks.getOrPut(e.first) { defaultArrayList() }.add(e.second)
                } else {
                    while (!queue.isEmpty) {
                        val e = queue.popOrNull() ?: break
                        if (e.second === CLOSE_MARKER) {
                            break@MAIN_WORKER_LOOP
                        }
                        tasks.getOrPut(e.first) { defaultArrayList() }.add(e.second)
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
                if (closedFlag.getValue()) {
                    break@MAIN_WORKER_LOOP
                }

                if (signal) {
                    continue
                    // wakeup by signal
                } else {
                    // wakeup by time
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

    override fun delay(delay: Duration, func: () -> Unit) {
        lock.synchronize {
            val endTime = startTime.elapsedNow() + delay
            queue.push((endTime to func).doFreeze())
            condition.signal()
        }
    }

//    override suspend fun <T> timeout(delay: Duration, func: suspend () -> T): T =
//        suspendCancellableCoroutine { con ->
//            val dispatcher = con.context[ContinuationInterceptor] as CoroutineDispatcher? ?: return@suspendCancellableCoroutine
//            val functionDone = AtomicBoolean(false)
//            func.startCoroutine(object : Continuation<T> {
//                override val context: CoroutineContext = con.context
//
//                override fun resumeWith(result: Result<T>) {
//                    if (functionDone.compareAndSet(false, true)) {
//                        con.resumeWith(result = result)
//                    }
//                }
//            })
//            val conRef = con.asReference()
//            delay(delay) {
//                if (functionDone.compareAndSet(false, true)) {
//                    dispatcher.dispatch(con.context, Runnable {
//
//                    })
//                    dispatcher.resume(
//                        result = Result.failure(TimeoutException()),
//                        continuation = conRef as Reference<Continuation<Any?>>
//                    )
//                } else {
//                    conRef.close()
//                }
//            }
//        }

    @Suppress("UNCHECKED_CAST")
    override suspend fun delay(delay: Duration) {
        suspendCancellableCoroutine<Unit> { con ->
            val dispatcher = con.context[ContinuationInterceptor] as CoroutineDispatcher?
            if (dispatcher == null) {
                con.resumeWithException(IllegalStateException("Can't find current ContinuationInterceptor"))
                return@suspendCancellableCoroutine
            }
            delay(delay) {
                dispatcher.dispatch(
                    con.context,
                    Runnable {
                        con.resumeWith(Result.success(Unit))
                    }
                )
            }
        }
    }

    override fun close() {
        if (closedFlag.getValue()) {
            throw ClosedException()
        }
        closedFlag.setValue(true)
        lock.synchronize {
            condition.signal()
        }
        queue.push((123.seconds to CLOSE_MARKER).doFreeze())
        worker.requestTermination().joinAndGetOrThrow()
    }
}
