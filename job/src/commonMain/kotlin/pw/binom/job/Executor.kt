package pw.binom.job

import pw.binom.io.Closeable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Executor : Closeable {
    override fun close() {
        executor.close()
    }

    private val executor = PlatfromExecutor()

    fun <T, R> execute(param: T, f: (T) -> R): Promise<R> = executor.execute(param, f)

    fun <R> execute(f: () -> R): Promise<R> = execute(Unit) {
        f()
    }
}
internal expect class PlatfromExecutor : Closeable {
    constructor()

    fun <T, R> execute(param: T, f: (T) -> R): Promise<R>
}

suspend fun <T> Promise<T>.await(): T =
        suspendCoroutine { c ->
            then({ c.resume(it) }, { c.resumeWithException(it) })
        }