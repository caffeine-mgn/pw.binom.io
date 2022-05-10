package pw.binom

import kotlin.coroutines.suspendCoroutine

class FeaturePromise<T> {
    private var onResume: ((Result<T>) -> Unit)? = null // = Stack<(Result<T>) -> Unit>()
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
