package pw.binom.network

import pw.binom.io.ClosedException

abstract class AbstractKey(override var attachment: Any?) : Selector.Key, Comparable<AbstractKey> {
    var connected = false

    protected abstract val selector: AbstractSelector
    abstract fun addSocket(raw: RawSocket)
    abstract fun removeSocket(raw: RawSocket)

//    val ptr = StableRef.create(this).asCPointer()

    private var _listensFlag = 0
    private var _closed = false

    init {
        NetworkMetrics.incSelectorKey()
    }

    override val closed: Boolean
        get() = _closed

    override fun compareTo(other: AbstractKey): Int = hashCode() - other.hashCode()

    protected fun checkClosed() {
        if (_closed) {
            throw ClosedException()
        }
    }

    abstract fun isSuccessConnected(nativeMode: Int): Boolean

    override var listensFlag: Int
        get() {
            return _listensFlag
        }
        set(value) {
            checkClosed()
            if (_listensFlag == value) {
                return
            }
            _listensFlag = value
            resetMode(value)
            selector.wakeup()
        }

    abstract fun resetMode(mode: Int)
    override fun close() {
        if (_closed) {
            return
        }
        NetworkMetrics.decSelectorKey()
        _closed = true
//            runCatching { attachmentReference?.close() }
        runCatching {
//            ptr.asStableRef<AbstractKey>().dispose()
        }
    }
}
