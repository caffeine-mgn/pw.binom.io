package pw.binom.network

import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import java.io.IOException
import java.nio.channels.NotYetConnectedException
import java.nio.channels.SocketChannel as JSocketChannel

actual class TcpClientSocketChannel actual constructor() : Channel {
    var native: JSocketChannel? = null
        internal set

    var key: JvmSelector.JvmKey? = null
        set(value) {
            field = value
            if (native != null) {
                key?.setNative(native!!)
            }
        }

    constructor(native: JSocketChannel) : this() {
        this.native = native
    }

    internal var blocking = false
    private fun get(): JSocketChannel {
        var native = native
        if (native == null) {
            native = JSocketChannel.open()
            native.configureBlocking(blocking)
            native.socket().tcpNoDelay = true
            this.native = native
            key?.setNative(native)
            return native
        }
        return native
    }

    actual fun setBlocking(value: Boolean) {
        native?.configureBlocking(value)
        blocking = value
    }

    actual fun connect(address: NetworkAddress) {
        val _native = address._native
        require(_native != null)
        get().connect(_native)
    }

    private var disconneced = false
    override fun read(dest: ByteBuffer): Int {
        if (disconneced) {
            return -1
        }
        val count = try {
            get().read(dest.native)
        } catch (e: IOException) {
            runCatching { get().close() }
            disconneced = true
            return -1
        } catch (e: NotYetConnectedException) {
            return 0
        }
        if (count < 0 || count == 0 && !get().isConnected) {
            runCatching { get().close() }
            return -1
        }
        return count
    }

    override fun close() {
        val socket = get()
//        runCatching { socket.shutdownInput() }
//        runCatching { socket.shutdownOutput() }
        socket.close()
    }

    override fun write(data: ByteBuffer): Int =
        try {
            val ret = get().write(data.native)
            if (ret < 0) {
                -1
            } else {
                ret
            }
        } catch (e: IOException) {
            close()
            -1
        }

    override fun flush() {
    }

    actual fun connect(fileName: String) {
        internalConnectToUnixSocket(fileName)
    }
}
