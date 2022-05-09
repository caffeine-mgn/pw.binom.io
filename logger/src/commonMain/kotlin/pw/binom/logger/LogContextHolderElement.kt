package pw.binom.logger

import pw.binom.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

internal class LogContextHolderElement(tags: Map<String, String> = emptyMap()) : CoroutineContext.Element {
    private val _tags = AtomicReference<Map<String, String>>(emptyMap())
    var tags: Map<String, String>
        get() = _tags.getValue()
        set(value) {
            _tags.setValue(value)
        }

    init {
        _tags.setValue(tags)
    }

    override val key: CoroutineContext.Key<LogContextHolderElement>
        get() = LogContextHolderElementKey
}
