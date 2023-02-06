package pw.binom.concurrency

/*
import pw.binom.FreezableFuture
import pw.binom.Future
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.collections.defaultMutableList
import kotlin.coroutines.*

class ExecutorServiceHolderElement(val executor: ExecutorService) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<ExecutorServiceHolderElement>
        get() = ExecutorServiceHolderKey
}

object ExecutorServiceHolderKey : CoroutineContext.Key<ExecutorServiceHolderElement>

// @Suppress("UNCHECKED_CAST")
// class CrossThreadCoroutineElement(val crossThreadCoroutine: CrossThreadCoroutine) : CoroutineContext.Element {
//    override val key: CoroutineContext.Key<CrossThreadCoroutineElement>
//        get() = CrossThreadCoroutineKey
// }
//
// object CrossThreadCoroutineKey : CoroutineContext.Key<CrossThreadCoroutineElement>

fun <P> asyncWithExecutor(executor: ExecutorService, f: suspend () -> P) =
    f.startCoroutine(object : Continuation<P> {
        override val context: CoroutineContext = EmptyCoroutineContext + ExecutorServiceHolderElement(executor)

        override fun resumeWith(result: Result<P>) {
        }
    })

suspend fun <T> ExecutorService.useInContext(f: suspend () -> T) {
    suspendCoroutine<T> { con ->
        f.startCoroutine(object : Continuation<T> {
            override val context: CoroutineContext = con.context + ExecutorServiceHolderElement(this@useInContext)

            override fun resumeWith(result: Result<T>) {
                con.resumeWith(result)
            }
        })
    }
}

class WorkerPool(size: Int = Worker.availableProcessors) {
    private class State(size: Int) {
        var interotped = AtomicBoolean(false)
        val tasks = AtomicInt(0)
        val queue = ConcurrentQueue<() -> Any?>()
    }

    private val state = State(size)
    private val list = Array(size) { Worker() }

    fun shutdown() {
        if (state.interotped.getValue()) {
            error("WorkerPool already has Interotped")
        }
        state.interotped.setValue(true)
        while (state.tasks.getValue() > 0 || list.any { it.taskCount > 0 }) {
            sleep(50)
        }
    }

    fun shutdownNow(): List<() -> Any?> {
        if (state.interotped.getValue()) {
            error("WorkerPool already has Interrupted")
        }
        state.interotped.setValue(true)
        val out = defaultMutableList<() -> Any?>(state.queue.size)

        while (!state.queue.isEmpty) {
            out += state.queue.pop()
        }
        return out
    }

    fun <T> submit(f: () -> T): Future<T> {
        if (state.interotped.getValue()) {
            throw IllegalStateException("WorkerPool already has Interrupted")
        }
        val future = FreezableFuture<T>()
        val freeWorker = list.find { it.taskCount == 0 }
        val status = state
        state.tasks.increment()
        if (freeWorker != null) {
            freeWorker.execute(
                ExecuteParams(
                    future = future,
                    f = f,
                    state = state,
                    worker = freeWorker,
                )
            ) {
                try {
                    it.future.resume(runCatching(it.f))
                } finally {
                    it.state.tasks.decrement()
                }
                if (freeWorker.taskCount > 0) {
                    while (true) {
                        val func = it.state.queue.popOrNull() ?: break
                        func()
                    }
                }
            }
        } else {
            state.queue.push(
                {
                    future.resume(runCatching(f))
                    status.tasks.decrement()
                }
            )
        }
        return future
    }

    private data class ExecuteParams<T>(
        val future: FreezableFuture<T>,
        val worker: Worker,
        val f: () -> T,
        val state: State
    )

//    override fun <T> startCoroutine(context: CoroutineContext, func: suspend () -> T): FreezableFuture<T> {
//        val future = FreezableFuture<T>()
//        submit {
//            WorkerImpl.current!!.startCoroutine(onDone = {
//                try {
//                    future.resume(it)
//                } catch (e: Throwable) {
//                    if (it.isFailure) {
//                        e.addSuppressed(it.exceptionOrNull()!!)
//                    }
//                    throw e
//                }
//            }, context = context, func = func)
//        }
//        return future
//    }

//    override fun <T> startCoroutine(
//        context: CoroutineContext,
//        continuation: CrossThreadContinuation<T>,
//        func: suspend () -> T
//    ) {
//        submit {
//            WorkerImpl.current!!.startCoroutine(
//                continuation = continuation,
//                context = context,
//                func = func
//            )
//        }
//    }

//    override fun <T> resume(continuation: Reference<Continuation<T>>, result: Result<T>) {
//        throw IllegalStateException("Can't resume Coroutine on Pool")
//    }

//    override fun dispatch(context: CoroutineContext, block: Runnable) {
//        submit { block.run() }
//    }
}
*/
