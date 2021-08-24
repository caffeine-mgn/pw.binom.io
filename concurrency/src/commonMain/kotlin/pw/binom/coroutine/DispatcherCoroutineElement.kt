package pw.binom.coroutine

import kotlin.coroutines.CoroutineContext

class DispatcherCoroutineElement(val dispatcher: Dispatcher) : CrossThreadCoroutineContextElement {
    override val key: CoroutineContext.Key<*>
        get() = DispatcherCoroutineKey
}