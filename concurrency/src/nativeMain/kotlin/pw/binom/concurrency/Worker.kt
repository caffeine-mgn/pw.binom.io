package pw.binom.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import pw.binom.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.coroutine.CrossThreadContinuation
import pw.binom.coroutine.DispatcherCoroutineElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker as KWorker
import kotlin.native.concurrent.freeze

@ThreadLocal
private var privateCurrentWorker: Worker? = null

actual class Worker actual constructor(name:String?) : CoroutineDispatcher() {
    private val nativeWorker = KWorker.start(errorReporting = true, name = name)
    private val _isInterrupted = AtomicBoolean(false)
    private var _taskCount by AtomicInt(0)
    actual val taskCount
        get() = _taskCount

    actual fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT> {
        val r = FreezableFuture<RESULT>()
        val nativeFeature = nativeWorker.execute(TransferMode.SAFE, getFunc(this, input, func, r)) {
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
            it.future.resume(result)
            result
        }
        return r
    }

    actual fun requestTermination(): Future<Unit> =
        FutureUnit(nativeWorker.requestTermination(true))

    actual val isInterrupted: Boolean
        get() = _isInterrupted.value

    init {
        freeze()
    }

    actual companion object {
        actual val current: Worker?
            get() = privateCurrentWorker

    }

    actual val id: Long
        get() = nativeWorker.id.toLong()

//    actual fun <T> startCoroutine(
//        onDone: (Result<T>) -> Unit,
//        context: CoroutineContext,
//        func: suspend () -> T,
//    ) {
//        val data = CoroutineStartData(
//            context = context,
//            func = func,
//            onDone = onDone,
//        )
//        execute(data) {
//            data.func.startCoroutine(object : Continuation<T> {
//                val dispatcherElement = DispatcherCoroutineElement(this@WorkerImpl)
//                override val context: CoroutineContext
//                    get() = data.context + dispatcherElement
//
//                override fun resumeWith(result: Result<T>) {
//                    it.onDone(result)
//                }
//            })
//        }
//    }

//    override fun <T> startCoroutine(context: CoroutineContext, func: suspend () -> T): FreezableFuture<T> {
//        val future = FreezableFuture<T>()
//        startCoroutine(
//            onDone = {
//                try {
//                    future.resume(it)
//                } catch (e: Throwable) {
//                    if (it.isFailure) {
//                        e.addSuppressed(it.exceptionOrNull()!!)
//                    }
//                    throw e
//                }
//            },
//            context = context,
//            func = func
//        )
//        return future
//    }

//    override fun <T> startCoroutine(
//        context: CoroutineContext,
//        continuation: CrossThreadContinuation<T>,
//        func: suspend () -> T
//    ) {
//        startCoroutine(
//            onDone = { continuation.resumeWith(it) },
//            context = context,
//            func = func
//        )
//    }

//    actual override fun <T> resume(continuation: Reference<Continuation<T>>, result: Result<T>) {
//        execute(result to continuation) {
//            val f = it.second.value
//            it.second.close()
//            f.resumeWith(it.first)
//        }
//    }


    override fun dispatch(context: CoroutineContext, block: Runnable) {
        this.execute { block.run() }
    }
}


private class InputData<DATA, RESULT>(
    val worker: Worker,
    val input: DATA,
    func: (DATA) -> RESULT,
    val future: FreezableFuture<RESULT>,
) {
    val func = ObjectTree { func }
}

private fun <DATA, RESULT> getFunc(
    worker: Worker,
    input: DATA,
    func: (DATA) -> RESULT,
    future: FreezableFuture<RESULT>,
): () -> InputData<DATA, RESULT> =
    {
        InputData(worker = worker, input = input, func = func, future = future)
    }.doFreeze()