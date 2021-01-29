package pw.binom

import kotlin.coroutines.*

class FeaturePromise<T> {
    private var onResume: ((Result<T>) -> Unit)? = null// = Stack<(Result<T>) -> Unit>()
    private var result: Result<T>? = null
    fun onResume(func: (Result<T>) -> Unit): FeaturePromise<T> {
        val result = this.result
        if (result != null)
            func(result)
        else
            onResume = func
        return this
    }

    fun resume(result: Result<T>) {
        check(this.result == null)
        this.result = result
        onResume?.invoke(result)
    }
}

suspend fun <T> FeaturePromise<T>.await(): T =
        suspendCoroutine {
            this.onResume { result ->
                it.resumeWith(result)
            }
        }

fun <P, T> (suspend (P) -> T).start(value: P): FeaturePromise<T> {
    val promise = FeaturePromise<T>()
    this.startCoroutine(value, object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            promise.resume(result)
        }
    })
    return promise
}

fun <T> (suspend () -> T).start() {
    this.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
        }
    })
}

fun <P> async(f: suspend () -> P) = f.start()

fun <T> (suspend () -> T).start2(): BaseFuture<T> {
    val promise = BaseFuture<T>()
    this.startCoroutine(object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            promise.resume(result)
        }
    })
    return promise
}

fun <P> async2(f: suspend () -> P) = f.start2()