package pw.binom.concurrency

import pw.binom.Future
import pw.binom.getOrException
import kotlin.coroutines.CoroutineContext

internal data class CoroutineStartData<T>(
    val context: CoroutineContext,
    val func: suspend () -> T,
    val onDone: (Result<T>) -> Unit
)

fun <T> Future<T>.join() {
    while (!isDone) {
        sleep(1)
    }
}

fun <T> Future<T>.joinAndGetOrThrow(): T {
    join()
    return getOrException()
}
