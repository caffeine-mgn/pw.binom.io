package pw.binom.network

abstract class AbstractSelector : Selector {
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

    protected abstract fun nativePrepare(mode: Int, connectable: Boolean, attachment: Any?): AbstractKey
    protected abstract fun nativeAttach(socket: NSocket, mode: Int, connectable: Boolean, attachment: Any?): AbstractKey
}

// internal expect fun epollCommonToNative(mode: Int): Int
// internal expect fun epollNativeToCommon(mode: Int): Int

internal inline operator fun Int.contains(value: Int): Boolean = value and this != 0
internal inline operator fun Int.contains(value: UInt): Boolean = value.toInt() in this
internal inline operator fun UInt.contains(value: Int): Boolean = value in this.toInt()
internal inline operator fun UInt.contains(value: UInt): Boolean = value.toInt() in this.toInt()

internal inline operator fun UShort.contains(value: Int): Boolean = value in toInt()
