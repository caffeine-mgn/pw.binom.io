package pw.binom.concurrency

import pw.binom.Future
import pw.binom.io.Closeable
import pw.binom.thread.FutureWrapper
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze

actual class StateHolder : Closeable {
    val worker = Worker.start()
    actual fun <T : Any> make(value: T): Future<Reference<T>> {
        val result = worker.execute(TransferMode.SAFE, { value }) {
            Result.success(it.asReference())
        }

        return FutureWrapper(result)
    }

    actual fun <T : Any, R> access(value: Reference<T>, func: (T) -> R): Future<R> =
            if (value.owner.same)
                Future.success(func(value.value))
            else
                FutureWrapper(
                        worker.execute(TransferMode.SAFE, { func.freeze() to value }) {
                            kotlin.runCatching {
                                it.first(it.second.value)
                            }
                        }
                )

    override fun close() {
        worker.requestTermination(false)
    }

}