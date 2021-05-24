package pw.binom.concurrency

import pw.binom.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import kotlin.coroutines.Continuation
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.Worker as NativeWorker

@ThreadLocal
private var privateCurrentWorker: Worker? = null

private class InputData<DATA, RESULT>(val worker: Worker, val input: DATA, func: (DATA) -> RESULT) {
    val func = ObjectTree { func }
}

private fun <DATA, RESULT> getFunc(worker: Worker, input: DATA, func: (DATA) -> RESULT): () -> InputData<DATA, RESULT> =
    {
        InputData(worker, input, func)
    }.doFreeze()

actual class Worker actual constructor(name: String?) : CrossThreadCoroutine, Executor {
    private val nativeWorker = NativeWorker.start(errorReporting = true, name = name)
    private val _isInterrupted = AtomicBoolean(false)
    private var _taskCount by AtomicInt(0)
    actual val taskCount
        get() = _taskCount

    actual fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT> {
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

    override fun coroutine(result: Result<Any?>, continuation: Reference<Continuation<Any?>>) {
        execute(result to continuation) {
            val f = it.second.value
            it.second.close()
            f.resumeWith(it.first)
        }
    }

    override fun execute(func: suspend () -> Unit) {
        execute(func.doFreeze()) {
            async2(it)
        }
    }
}