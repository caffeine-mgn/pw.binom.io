package pw.binom.concurrency

import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext marked using [CrossThreadCoroutineContext] will be pass to other thead when coroutine will start on this other thread
 */
interface CrossThreadCoroutineContext : CoroutineContext