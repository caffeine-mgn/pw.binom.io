package pw.binom.job

import pw.binom.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal actual class PlatfromExecutor : Closeable {
    override fun close() {
        native.shutdown()
        native.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
    }

    private val native = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    actual fun <T, R> execute(param: T, f: (T) -> R): Promise<R> {
        val out = Promise<R>()
        native.submit {
            try {
                out.resume(f(param))
            } catch (e: Throwable) {
                out.exception(e)
            }
        }
        return out
    }
}