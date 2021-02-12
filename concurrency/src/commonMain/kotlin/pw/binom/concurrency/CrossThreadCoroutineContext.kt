package pw.binom.concurrency

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * CoroutineContext marked using [CrossThreadCoroutineContext] will be pass to other thead when coroutine will start on this other thread
 */
interface CrossThreadCoroutineContext : CoroutineContext

fun CoroutineContext.buildCrossThreadContext() =
    fold(EmptyCoroutineContext as CoroutineContext) { f, s ->
        if (s is CrossThreadCoroutineContext) {
            f + s
        } else {
            f
        }
    }