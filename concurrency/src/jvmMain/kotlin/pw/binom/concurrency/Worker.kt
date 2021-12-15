@file:JvmName("WorkerJvm")
package pw.binom.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import pw.binom.Future
import pw.binom.NonFreezableFuture
import pw.binom.atomic.AtomicInt
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val currentWorker = ThreadLocal<Worker>()
private val idSeq = AtomicLong(0)

private object WorkerThreadFactory1 : ThreadFactory {
    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r)
        thread.isDaemon = true
        return thread
    }
}

private class WorkerThreadFactoryWithName(val name: String) : ThreadFactory {
    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r)
        thread.isDaemon = true
        thread.name = name
        return thread
    }
}

actual class Worker actual constructor(name: String?) : CoroutineDispatcher() {
    private val _id = idSeq.incrementAndGet()
    private val worker = Executors.newSingleThreadExecutor(
        if (name == null) WorkerThreadFactory1 else WorkerThreadFactoryWithName(name)
    )

    init {
        worker.submit {
            currentWorker.set(this)
        }
    }

    @OptIn(ExperimentalTime::class)
    actual fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT> {
        if (worker.isShutdown or worker.isTerminated) {
            throw IllegalStateException("Worker already terminated")
        }
        val future = worker.submit(
            Callable {
                _taskCount.increment()
                val gg = measureTimedValue { runCatching { func(input) } }
                _taskCount.decrement()
                gg.value
            }
        )
        return FutureWrapper(future)
    }

    actual fun requestTermination(): Future<Unit> {
        val future = NonFreezableFuture<Unit>()
        worker.submit {
            currentWorker.set(null)
            future.resume(Result.success(Unit))
        }
        worker.shutdown()
        return future
    }

    actual val isInterrupted: Boolean
        get() = worker.isShutdown

    actual companion object {
        actual val current: Worker?
            get() = currentWorker.get()
    }

    actual val id: Long
        get() = _id
    private var _taskCount = AtomicInt(0)
    actual val taskCount
        get() = _taskCount.value

//    override fun coroutine(result: Result<Any?>, continuation: Reference<Continuation<Any?>>) {
//        execute(result to continuation) {
//            val f = it.second.value
//            it.second.close()
//            f.resumeWith(it.first)
//        }
//    }
/*
    fun execute(func: suspend () -> Unit) {
        worker.submit {
            _taskCount.increment()
            runCatching {
                async2(func)
            }
            _taskCount.decrement()
        }
    }

    actual fun <T> startCoroutine(
        onDone: (Result<T>) -> Unit,
        context: CoroutineContext,
        func: suspend () -> T,
    ) {
        val data = CoroutineStartData(
            context = context,
            func = func,
            onDone = onDone,
        )
        execute(data) {
            data.func.startCoroutine(object : Continuation<T> {
                val dispatcherElement = DispatcherCoroutineElement(this@WorkerImpl)
                override val context: CoroutineContext
                    get() = data.context + dispatcherElement

                override fun resumeWith(result: Result<T>) {
                    data.onDone(result)
                }
            })
        }
    }

    override fun <T> startCoroutine(context: CoroutineContext, func: suspend () -> T): FreezableFuture<T> {
        val future = FreezableFuture<T>()
        startCoroutine(
            onDone = {
                try {
                    future.resume(it)
                } catch (e: Throwable) {
                    if (it.isFailure) {
                        e.addSuppressed(it.exceptionOrNull()!!)
                    }
                    throw e
                }
            },
            context = context,
            func = func,
        )
        return future
    }

    override fun <T> startCoroutine(
        context: CoroutineContext,
        continuation: CrossThreadContinuation<T>,
        func: suspend () -> T
    ) {
        val future = FreezableFuture<T>()
        startCoroutine(
            onDone = {
                continuation.resumeWith(it)
            },
            context = context,
            func = func,
        )
    }

    actual override fun <T> resume(continuation: Reference<Continuation<T>>, result: Result<T>) {
        execute(result to continuation) {
            val f = it.second.value
            it.second.close()
            f.resumeWith(it.first)
        }
    }
    */

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        this.worker.submit(block)
    }
}

actual val Worker.Companion.availableProcessors: Int
    get() = Runtime.getRuntime().availableProcessors()