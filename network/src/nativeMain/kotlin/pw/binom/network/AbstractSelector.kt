package pw.binom.network

abstract class AbstractSelector : Selector {
    override fun attach(socket: TcpClientSocketChannel, attachment: Any?, mode: Int): AbstractKey {
        val key = if (socket.native == null) {
            nativePrepare(mode = mode, attachment = attachment, connectable = socket.connectable)
        } else {
            nativeAttach(
                socket = socket.nNative!!,
                mode = mode,
                connectable = true,
                attachment = attachment,
            )
        }

        if (!socket.connectable) {
            key.connected = true
        }
        key.socket = socket
        return key
    }

    override fun attach(socket: TcpServerSocketChannel, attachment: Any?, mode: Int): AbstractKey {
        val key = if (socket.native == null) {
            nativePrepare(
                mode = mode,
                attachment = attachment,
                connectable = false,
            )
        } else {
            nativeAttach(
                socket = socket.nNative!!,
                mode = mode,
                connectable = false,
                attachment = attachment,
            )
        }
        key.socket = socket
        return key
    }

    override fun attach(socket: UdpSocketChannel, attachment: Any?, mode: Int): AbstractKey {
        val key = nativePrepare(
            mode = mode,
            connectable = false,
            attachment = attachment
        )
        key.socket = socket
        return key
    }

    interface NativeKeyEvent {
        val key: AbstractKey
        val mode: Int
    }

    override fun wakeup() {
        TODO("Not yet implemented")
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
