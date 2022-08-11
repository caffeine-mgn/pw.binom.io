package pw.binom.network

import pw.binom.io.ByteBuffer
import pw.binom.io.Channel
import java.io.IOException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.NotYetConnectedException
import kotlin.io.path.Path
import java.nio.channels.SocketChannel as JSocketChannel

actual class TcpClientSocketChannel actual constructor() : Channel {
    var native: JSocketChannel? = null
        private set

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

    private fun getUnixSocket(): JSocketChannel {
        var native = native
        if (native == null) {
            native = JSocketChannel.open(StandardProtocolFamily.UNIX)
            native.configureBlocking(blocking)
            this.native = native
            key?.setNative(native)
            return native
        }
        return native
    }

    private var blocking = false
    private fun get(): JSocketChannel {
        var native = native
        if (native == null) {
            native = JSocketChannel.open()
            native.configureBlocking(blocking)
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
        get().close()
    }

    override fun write(data: ByteBuffer): Int =
        try {
            val ret = get().write(data.native)
            if (ret < 0) {
                throw SocketClosedException()
            }
            ret
        } catch (e: IOException) {
            throw SocketClosedException()
        }

    override fun flush() {
    }

    actual fun connect(fileName: String) {
        getUnixSocket().connect(UnixDomainSocketAddress.of(Path(fileName)))
    }
}
