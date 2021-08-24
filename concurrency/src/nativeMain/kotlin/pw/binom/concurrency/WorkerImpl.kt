package pw.binom.concurrency

import pw.binom.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.coroutine.CrossThreadContinuation
import pw.binom.coroutine.Dispatcher
import pw.binom.coroutine.DispatcherCoroutineElement
import pw.binom.coroutine.Executor
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.Worker as NativeWorker

@ThreadLocal
private var privateCurrentWorker: WorkerImpl? = null

private class InputData<DATA, RESULT>(val worker: WorkerImpl, val input: DATA, func: (DATA) -> RESULT) {
    val func = ObjectTree { func }
}

private fun <DATA, RESULT> getFunc(
    worker: WorkerImpl,
    input: DATA,
    func: (DATA) -> RESULT
): () -> InputData<DATA, RESULT> =
    {
        InputData(worker, input, func)
    }.doFreeze()

actual class WorkerImpl(name: String?) : Executor, Worker, Dispatcher {
    private val nativeWorker = NativeWorker.start(errorReporting = true, name = name)
    private val _isInterrupted = AtomicBoolean(false)
    private var _taskCount by AtomicInt(0)
    actual override val taskCount
        get() = _taskCount

    actual override fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT> {
        val nativeFeature = nativeWorker.execute(TransferMode.SAFE, getFunc(this, input, func)) {
            initRuntimeIfNeeded()
            val ff = it.func.attach()
            privateCurrentWorker = it.worker
            it.worker._taskCount++
            val result = try {
                Result.success(ff(it.input))
            } catch (e: Throwable) {
                Result.failure(e)
            }
            it.worker._taskCount--
            privateCurrentWorker = null
            result
        }
        return FutureWrapper(nativeFeature)
    }

    actual override fun requestTermination(): Future<Unit> =
        FutureUnit(nativeWorker.requestTermination(true))

    actual override val isInterrupted: Boolean
        get() = _isInterrupted.value

    init {
        freeze()
    }

    actual companion object {
        actual val current: WorkerImpl?
            get() = privateCurrentWorker

    }

    actual override val id: Long
        get() = nativeWorker.id.toLong()

    override fun execute(func: suspend () -> Unit) {
        execute(func.doFreeze()) {
            async2(it)
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
                    it.onDone(result)
                }
            })
        }
    }

    override fun <T> startCoroutine(context: CoroutineContext, func: suspend () -> T): FreezableFuture<T> {
        val future = FreezableFuture<T>()
        startCoroutine(
            onDone = { future.resume(it) },
            context = context,
            func = func
        )
        return future
    }

    override fun <T> startCoroutine(
        context: CoroutineContext,
        continuation: CrossThreadContinuation<T>,
        func: suspend () -> T
    ) {
        startCoroutine(
            onDone = { continuation.coroutine(it) },
            context = context,
            func = func
        )
    }

    actual override fun <T> resume(continuation: Reference<Continuation<T>>, result: Result<T>) {
        execute(result to continuation) {
            val f = it.second.value
            it.second.close()
            f.resumeWith(it.first)
        }
    }
}

actual fun Worker.Companion.create(name: String?): Worker = WorkerImpl(name)