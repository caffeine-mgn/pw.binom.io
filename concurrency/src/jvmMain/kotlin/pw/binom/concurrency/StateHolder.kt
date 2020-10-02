package pw.binom.concurrency

import pw.binom.Future
import pw.binom.io.Closeable
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future as JFuture

actual class StateHolder : Closeable {
    private val worker = Executors.newSingleThreadExecutor()
    actual fun <T : Any> make(value: () -> T): Future<Reference<T>> {
        val result = worker.submit(Callable {
            Result.success(value().asReference())
        })

        return FutureWrapper(result)
    }

    actual fun <T : Any, R> access(value: Reference<T>, func: (T) -> R): Future<R> {
        val result = worker.submit(Callable {
            kotlin.runCatching { func(value.value) }
        })

        return FutureWrapper(result)
    }

    override fun close() {
        worker.shutdown()
    }

}