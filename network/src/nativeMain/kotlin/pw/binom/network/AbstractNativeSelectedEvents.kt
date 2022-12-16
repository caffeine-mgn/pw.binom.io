package pw.binom.network

import pw.binom.collections.defaultMutableList

abstract class AbstractNativeSelectedEvents : SelectedEventsOld {

    protected abstract val nativeSelectedKeys: Iterator<AbstractNativeSelector.NativeKeyEvent>
    internal abstract fun internalResetFlags()

    protected val closedKeys2: MutableList<SelectorOld.Key> = defaultMutableList()

    private val selected = object : Iterator<SelectorOld.KeyEvent> {
        override fun hasNext(): Boolean = nativeSelectedKeys.hasNext()
        private val event = object : SelectorOld.KeyEvent {
            override lateinit var key: SelectorOld.Key
            override var mode: Int = 0

            override fun toString(): String = selectorModeToString(mode)
        }

        override fun next(): SelectorOld.KeyEvent {
            val e = nativeSelectedKeys.next()

            if (!e.key.connected) {
                if (e.mode and SelectorOld.EVENT_CONNECTED != 0) {
                    e.key.connected = true
                    event.key = e.key
                    event.mode = SelectorOld.EVENT_CONNECTED or SelectorOld.OUTPUT_READY
                    return event
                } else {
                    try {
                        event.key = e.key
                        event.mode = SelectorOld.EVENT_ERROR
                        return event
                    } finally {
                        if (!e.key.closed) {
                            e.key.close()
                        }
                    }
                }
            } else {
                event.key = e.key
                event.mode = e.mode
                return event
            }
        }
    }
    private var closedKeysCursor = 0
    private val commonIterator = object : Iterator<SelectorOld.KeyEvent> {
        private var closedKeyEvent1 = object : SelectorOld.KeyEvent {
            override val key: SelectorOld.Key
                get() = internalKey!!
            var internalKey: SelectorOld.Key? = null

            override val mode: Int
                get() = SelectorOld.EVENT_ERROR
        }

        override fun hasNext(): Boolean {
            if (selected.hasNext()) {
                return true
            }
            if (closedKeysCursor < closedKeys2.size) {
                return true
            }
            return false
        }

        override fun next(): SelectorOld.KeyEvent {
            if (selected.hasNext()) {
                return selected.next()
            }
            if (closedKeysCursor >= closedKeys2.size) {
                throw NoSuchElementException()
            }
            val closedKey = closedKeys2[closedKeysCursor++]
            closedKeyEvent1.internalKey = closedKey
            return closedKeyEvent1
        }
    }

    protected abstract fun resetIterator()

    override fun iterator(): Iterator<SelectorOld.KeyEvent> {
        resetIterator()
        closedKeys2.clear()
        closedKeysCursor = 0
        return commonIterator
    }
}
