package pw.binom.concurrency

// import pw.binom.io.Closeable
// import kotlin.time.Duration
//
// @Deprecated(message = "Not use it. Will be deleted")
// interface DeadlineTimer : Closeable {
//
//    /**
//     * @param delay delay in milliseconds
//     * @param func function for call after [delay]
//     */
//    fun delay(delay: Duration, func: () -> Unit)
//    suspend fun delay(delay: Duration)
//
//    companion object {
//        fun create(errorProcessing: ((Throwable) -> Unit)? = null) =
//            DeadlineTimerImpl(errorProcessing = errorProcessing)
//    }
// }

/*
private val CLOSE_MARKER: () -> Unit = {}

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalTime::class)
abstract class AbstractDeadlineTimer(val errorProcessing: ((Throwable) -> Unit)? = null) : Closeable {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private val worker = Worker()
    private val startTime = TimeSource.Monotonic.markNow()
    private val queue = ConcurrentQueue<Pair<Duration, () -> Unit>>()
    private val closedFlag = AtomicBoolean(false)
    val tasks = TreeMap<Duration, MutableList<() -> Unit>>()

    protected abstract fun errorProcessing(e: Throwable)

    init {
        worker.execute(Unit) {
            MAIN_WORKER_LOOP@ while (!closedFlag.getValue()) {
                if (tasks.isEmpty() && queue.isEmpty) {
                    val e = queue.popBlocked()
                    if (e.second === CLOSE_MARKER) {
                        break
                    }
                    tasks.getOrPut(e.first) { defaultMutableList() }.add(e.second)
                } else {
                    while (!queue.isEmpty) {
                        val e = queue.popOrNull() ?: break
                        if (e.second === CLOSE_MARKER) {
                            break@MAIN_WORKER_LOOP
                        }
                        tasks.getOrPut(e.first) { defaultMutableList() }.add(e.second)
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

    fun delay(delay: Duration, func: () -> Unit) {
        lock.synchronize {
            val endTime = startTime.elapsedNow() + delay
            queue.push((endTime to func))
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
        queue.push((123.seconds to CLOSE_MARKER))
        worker.requestTermination().joinAndGetOrThrow()
    }
}
*/
