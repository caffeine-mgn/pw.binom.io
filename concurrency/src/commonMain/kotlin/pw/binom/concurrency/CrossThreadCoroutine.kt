package pw.binom.concurrency

import kotlin.coroutines.Continuation

/**
 * Object must be frozen, because it will be pass to other thread
 */
fun interface CrossThreadCoroutine {
    fun coroutine(result: Result<Any?>,continuation:Reference<Continuation<Any?>>)
}