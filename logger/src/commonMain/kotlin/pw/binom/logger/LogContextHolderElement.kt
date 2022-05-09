package pw.binom.logger

import pw.binom.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

internal class LogContextHolderElement(tags: Map<String, String> = emptyMap()) : CoroutineContext.Element {
    private val _tags = AtomicReference<Map<String, String>>(emptyMap())
    val tags
        get() = _tags.getValue()

    init {
        _tags.setValue(tags)
    }

    override val key: CoroutineContext.Key<LogContextHolderElement>
        get() = LogContextHolderElementKey
}
