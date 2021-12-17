package pw.binom.coroutine

import kotlin.coroutines.CoroutineContext

/**
 * CoroutineContext marked using [CrossThreadCoroutineContextElement] will be pass to other thread when coroutine will start on this other thread
 */
@Deprecated(message = "Not use it!")
interface CrossThreadCoroutineContextElement : CoroutineContext.Element {
    fun fork(): CrossThreadCoroutineContextElement = this
}