package pw.binom.logger

import pw.binom.atomic.AtomicReference
import pw.binom.coroutine.CrossThreadCoroutineContextElement
import pw.binom.doFreeze
import kotlin.coroutines.CoroutineContext

internal class LogContextHolderElement(tags: Map<String, String> = emptyMap()) : CrossThreadCoroutineContextElement,
    CoroutineContext.Element {
    var tags by AtomicReference<Map<String, String>>(emptyMap())

    override fun fork(): CrossThreadCoroutineContextElement =
        LogContextHolderElement(tags)

    init {
        this.tags = tags.doFreeze()
    }

    override val key: CoroutineContext.Key<LogContextHolderElement>
        get() = LogContextHolderElementKey
}