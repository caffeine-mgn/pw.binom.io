package pw.binom.network

import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import pw.binom.io.ClosedException

abstract class AbstractSelector : Selector {
    abstract class AbstractKey(attachment: Any?) : Selector.Key {
        var connected = false

        //        private val attachmentReference = attachment?.asReference()
        override var attachment: Any? = attachment
        abstract fun addSocket(raw: RawSocket)
        abstract fun removeSocket(raw: RawSocket)

        //            get() = attachmentReference?.value
        val ptr = StableRef.create(this).asCPointer()
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
                ptr.asStableRef<AbstractKey>().dispose()
                attachment = null
            }
        }
    }

    override fun attach(socket: TcpClientSocketChannel, mode: Int, attachment: Any?): AbstractKey {
        val key = if (socket.native == null) {
            nativePrepare(mode = mode, attachment = attachment, connectable = socket.connectable)
        } else {
            nativeAttach(
                socket.native!!,
                mode,
                true,
                attachment
            )
        }

        if (!socket.connectable) {
            key.connected = true
        }
        socket.key = key
        return key
    }

    override fun attach(socket: TcpServerSocketChannel, mode: Int, attachment: Any?): AbstractKey {
        val key = if (socket.native == null) {
            nativePrepare(mode = mode, attachment = attachment, connectable = false)
        } else {
            nativeAttach(
                socket.native!!,
                mode,
                false,
                attachment
            )
        }
        socket.key = key
        return key
    }

    override fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?): AbstractKey {
        val key = nativePrepare(
            mode = mode,
            connectable = false,
            attachment = attachment
        )
        socket.key = key
        return key
    }

    interface NativeKeyEvent {
        val key: AbstractKey
        val mode: Int
    }

    protected abstract val nativeSelectedKeys: Iterator<NativeKeyEvent>

    protected abstract fun nativePrepare(mode: Int, connectable: Boolean, attachment: Any?): AbstractKey
    protected abstract fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey
    protected abstract fun nativeSelect(timeout: Long, func: (AbstractKey, mode: Int) -> Unit): Int
    protected abstract fun nativeSelect(timeout: Long)
}

// internal expect fun epollCommonToNative(mode: Int): Int
// internal expect fun epollNativeToCommon(mode: Int): Int

internal inline operator fun Int.contains(value: Int): Boolean = value and this != 0
internal inline operator fun Int.contains(value: UInt): Boolean = value.toInt() in this
internal inline operator fun UInt.contains(value: Int): Boolean = value in this.toInt()
internal inline operator fun UInt.contains(value: UInt): Boolean = value.toInt() in this.toInt()

internal inline operator fun UShort.contains(value: Int): Boolean = value in toInt()
