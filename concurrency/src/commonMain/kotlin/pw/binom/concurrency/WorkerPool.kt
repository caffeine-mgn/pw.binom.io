package pw.binom.concurrency

import pw.binom.FreezableFuture
import pw.binom.Future
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.doFreeze
import pw.binom.popOrNull
import kotlin.coroutines.*

class ExecutorServiceHolderElement(val executor: ExecutorService) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<ExecutorServiceHolderElement>
        get() = ExecutorServiceHolderKey
}

object ExecutorServiceHolderKey : CoroutineContext.Key<ExecutorServiceHolderElement>

@Suppress("UNCHECKED_CAST")
class CrossThreadCoroutineElement(val crossThreadCoroutine: CrossThreadCoroutine) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<CrossThreadCoroutineElement>
        get() = CrossThreadCoroutineKey
}

object CrossThreadCoroutineKey : CoroutineContext.Key<CrossThreadCoroutineElement>

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

fun ExecutorService.submitAsync(context: CoroutineContext = EmptyCoroutineContext, func: suspend () -> Unit) {
    func.doFreeze()
    submit {
        val f = func
        f.startCoroutine(object : Continuation<Unit> {
            override val context: CoroutineContext = context + CrossThreadCoroutineElement(Worker.current!!)

            override fun resumeWith(result: Result<Unit>) {
            }
        })
    }
}

class WorkerPool(size: Int = Worker.availableProcessors) : ExecutorService {
    private class State(size: Int) {
        var interotped = AtomicBoolean(false)
        val stoped = AtomicInt(size)
        val queue = ConcurrentQueue<() -> Any?>()

        init {
            doFreeze()
        }
    }

    private val state = State(size)
    private val list = Array(size) { Worker() }

    fun shutdown() {
        if (state.interotped.value) {
            throw IllegalStateException("WorkerPool already has Interotped")
        }
        state.interotped.value = true
        while (state.stoped.value != 0) {
            Worker.sleep(50)
        }
    }

    fun shutdownNow(): List<() -> Any?> {
        if (state.interotped.value) {
            throw IllegalStateException("WorkerPool already has Interrupted")
        }
        state.interotped.value = true
        val out = ArrayList<() -> Any?>(state.queue.size)
        while (!state.queue.isEmpty) {
            out += state.queue.pop()
        }
        return out
    }

    override fun <T> submit(f: () -> T): Future<T> {
        val future = FreezableFuture<T>()
        val freeWorker = list.find { it.taskCount == 0 }
        if (freeWorker != null) {
            freeWorker.execute(
                ExecuteParams(
                    future = future,
                    f = f,
                    state = state,
                    worker = freeWorker,
                ).doFreeze()
            ) {
                it.future.resume(runCatching(it.f))
                if (freeWorker.taskCount == 1) {
                    while (true) {
                        val func = it.state.queue.popOrNull() ?: break
                        func()
                    }
                }
            }
        } else {
            state.queue.push({
                future.resume(runCatching(f))
            }.doFreeze())
        }
        return future
    }

    private data class ExecuteParams<T>(val future: FreezableFuture<T>, val worker: Worker, val f: () -> T, val state: State)

    init {
        doFreeze()
    }
}