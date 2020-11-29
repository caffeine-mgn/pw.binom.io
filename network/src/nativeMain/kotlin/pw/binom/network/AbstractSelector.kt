package pw.binom.network

import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.concurrency.asReference

abstract class AbstractSelector : Selector {
    abstract class AbstractKey(attachment: Any?, val socket: NSocket) : Selector.Key {
        var connected by AtomicBoolean(false)
        private val attachmentReference = attachment?.asReference()
        override val attachment: Any?
            get() = attachmentReference?.value
        var ptr = StableRef.create(this).asCPointer()
        override var eventsFlag by AtomicInt(0)
        private var _listensFlag by AtomicInt(0)

        abstract fun isSuccessConnected(nativeMode: Int): Boolean

        override var listensFlag: Int
            get() = _listensFlag
            set(value) {
                _listensFlag = value
                resetMode(epollCommonToNative(value))
            }

        abstract fun resetMode(mode: Int)
        override fun close() {
            attachmentReference?.close()
            ptr.asStableRef<AbstractKey>().dispose()
        }
    }

    override fun attach(socket: TcpClientSocketChannel, mode: Int, attachment: Any?) =
        nativeAttach(
            socket.native,
            epollCommonToNative(mode),
            true,
            attachment
        )

    override fun attach(socket: TcpServerSocketChannel, mode: Int, attachment: Any?) =
        nativeAttach(
            socket.native,
            epollCommonToNative(mode),
            false,
            attachment
        )

    override fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?) =
        nativeAttach(
            socket.native,
            epollCommonToNative(mode),
            false,
            attachment
        )


    override fun select(timeout: Long, func: (Selector.Key, mode: Int) -> Unit): Int =
        nativeSelect(timeout) { key, nativeMode ->
            if (!key.connected) {
                if (key.isSuccessConnected(nativeMode)) {
                    key.connected = true
                    func(key, Selector.EVENT_CONNECTED or Selector.EVENT_EPOLLOUT)
                    key.resetMode(epollCommonToNative(key.listensFlag))
                } else {
                    func(key, Selector.EVENT_ERROR)
                    key.close()
                }
                return@nativeSelect
            }

            func(key, epollNativeToCommon(nativeMode))
        }


    protected abstract fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey
    protected abstract fun nativeSelect(timeout: Long, func: (AbstractKey, mode: Int) -> Unit): Int
}

internal expect fun epollCommonToNative(mode: Int): Int
internal expect fun epollNativeToCommon(mode: Int): Int

internal inline operator fun Int.contains(value: Int): Boolean = value and this != 0
internal inline operator fun Int.contains(value: UInt): Boolean = value.toInt() in this
internal inline operator fun UInt.contains(value: Int): Boolean = value in this.toInt()
internal inline operator fun UInt.contains(value: UInt): Boolean = value.toInt() in this.toInt()