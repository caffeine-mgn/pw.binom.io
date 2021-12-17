package pw.binom.coroutine

import kotlin.coroutines.CoroutineContext

@Deprecated(message = "Not use it!")
class DispatcherCoroutineElement(val dispatcher: Dispatcher) : CrossThreadCoroutineContextElement {
    override val key: CoroutineContext.Key<*>
        get() = DispatcherCoroutineKey
}