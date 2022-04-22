package pw.binom.network

import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import pw.binom.io.ClosedException

abstract class AbstractKey(attachment: Any?) : Selector.Key {
    var connected = false

    protected abstract val selector: AbstractSelector
    override var attachment: Any? = attachment
    abstract fun addSocket(raw: RawSocket)
    abstract fun removeSocket(raw: RawSocket)

//    val ptr = StableRef.create(this).asCPointer()

    private var _listensFlag = 0
    private var _closed = false

    override val closed: Boolean
        get() = _closed

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
        }

    abstract fun resetMode(mode: Int)
    override fun close() {
        if (_closed) {
            return
        }
        _closed = true
//            runCatching { attachmentReference?.close() }
        runCatching {
//            ptr.asStableRef<AbstractKey>().dispose()
            attachment = null
        }
    }
}
