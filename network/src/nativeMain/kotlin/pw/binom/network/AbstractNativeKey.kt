package pw.binom.network

import pw.binom.io.ClosedException

abstract class AbstractNativeKey(override var attachment: Any?) : SelectorOld.Key, Comparable<AbstractNativeKey> {
    internal var connected = false

    private var internalSocket: NetworkChannel? = null

//    internal fun internalCleanSocket() {
//        internalSocket = null
//    }

    internal fun setNetworkChannel(channel: NetworkChannel) {
        if (internalSocket === channel) {
            return
        }
        checkClosed()
        check(internalSocket == null) { "Key already has reference to NetworkChannel" }
        internalSocket = channel
        channel.setKey(this)
    }

    internal val channel: NetworkChannel
        get() = internalSocket ?: error("Channel not set at now")

    //    var socket: NetworkChannel?
//        get() = internalSocket
//        set(value) {
//            check(internalSocket == null) { "Key already has reference to NetworkChannel" }
//            if (internalSocket === value) {
//                return
//            }
//            internalSocket?.removeKey(this)
//            internalSocket = value
//            value?.addKey(this)
//        }
    abstract override val selector: AbstractNativeSelector

    abstract fun setRaw(raw: RawSocket)

//    val ptr = StableRef.create(this).asCPointer()

    private var _listensFlag = 0
    private var _closed = false

    init {
        NetworkMetrics.incSelectorKey()
    }

    override val closed: Boolean
        get() = _closed

    internal fun internalResetFlags() {
        _listensFlag = 0
    }

    override fun compareTo(other: AbstractNativeKey): Int = hashCode() - other.hashCode()

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
//            selector.wakeup()
        }

    abstract fun resetMode(mode: Int)

    protected fun internalClose(): Boolean {
        if (closed) {
            return false
        }
        NetworkMetrics.decSelectorKey()
        internalSocket?.keyClosed()
        _closed = true
        return true
    }
}
