package pw.binom.network

abstract class AbstractNativeSelectedEvents : SelectedEvents {

    protected abstract val nativeSelectedKeys: Iterator<AbstractSelector.NativeKeyEvent>
    internal abstract fun internalResetFlags()

    private val selected = object : Iterator<Selector.KeyEvent> {
        override fun hasNext(): Boolean = nativeSelectedKeys.hasNext()
        private val event = object : Selector.KeyEvent {
            override lateinit var key: Selector.Key
            override var mode: Int = 0

            override fun toString(): String = selectorModeToString(mode)
        }

        override fun next(): Selector.KeyEvent {
            val e = nativeSelectedKeys.next()

            if (!e.key.connected) {
                if (e.mode and Selector.EVENT_CONNECTED != 0) {
                    e.key.connected = true
                    event.key = e.key
                    event.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                    return event
                } else {
                    try {
                        event.key = e.key
                        event.mode = Selector.EVENT_ERROR
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

    protected abstract fun resetIterator()

    override fun iterator(): Iterator<Selector.KeyEvent> {
        resetIterator()
        return selected
    }
}
