package pw.binom.concurrency

import pw.binom.Future
import pw.binom.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.Future as JFuture

actual class StateHolder : Closeable {
    private val worker = Executors.newSingleThreadExecutor()
    actual fun <T : Any> make(value: ()->T): Future<Reference<T>> {
        val result: JFuture<Result<Reference<T>>> = worker.submit {
            Result.success(value().asReference())
        } as JFuture<Result<Reference<T>>>

        return FutureWrapper(result)
    }

    actual fun <T : Any, R> access(value: Reference<T>, func: (T) -> R): Future<R> {
        val result: JFuture<Result<R>> = worker.submit {
            func(value.value)
        } as JFuture<Result<R>>

        return FutureWrapper(result)
    }

    override fun close() {
        worker.shutdown()
    }

}