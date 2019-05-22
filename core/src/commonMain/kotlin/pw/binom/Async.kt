package pw.binom

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

fun <P, T> (suspend (P) -> T).start(value: P) {
    this.startCoroutine(value, object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            result.getOrThrow()
        }
    })
}

fun <T> (suspend () -> T).start() {
    this.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            result.getOrThrow()
        }
    })
}

fun <P> async(f: suspend () -> P) = f.start()