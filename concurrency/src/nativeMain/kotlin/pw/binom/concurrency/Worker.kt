package pw.binom.concurrency

import pw.binom.Future
import pw.binom.atomic.AtomicBoolean
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.freeze
import kotlin.native.concurrent.Worker as NativeWorker

@ThreadLocal
private var privateCurrentWorker: Worker? = null

private data class InputData<DATA, RESULT>(val worker: Worker, val input: DATA, val func: (DATA) -> RESULT)

actual class Worker actual constructor(name: String?) {
    private val nativeWorker = NativeWorker.start(errorReporting = true, name = name)
    private val _isInterrupted = AtomicBoolean(false)

    actual fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT> {

        val ind = InputData(this, input, func)
        ind.freeze()
        val nativeFeature = nativeWorker.execute(TransferMode.SAFE, { ind }) {
            initRuntimeIfNeeded()
            privateCurrentWorker = it.worker
            val result = runCatching { it.func(it.input) }
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
}