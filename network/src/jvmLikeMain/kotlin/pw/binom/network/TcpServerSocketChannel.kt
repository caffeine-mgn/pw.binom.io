package pw.binom.network

import pw.binom.io.Closeable
import java.net.InetSocketAddress
import java.net.BindException as JBindException
import java.nio.channels.ServerSocketChannel as JServerSocketChannel

actual class TcpServerSocketChannel : Closeable {
    var native: JServerSocketChannel? = null
        internal set
    var key: JvmSelector.JvmKey? = null
        set(value) {
            field = value
            if (native != null) {
                key?.setNative(native!!)
            }
        }

    private fun get(): JServerSocketChannel {
        var native = native
        if (native == null) {
            native = JServerSocketChannel.open()
            native.configureBlocking(blocking)
            this.native = native
            key?.setNative(native)
            return native
        }
        return native
    }

    actual fun accept(address: NetworkAddress.Mutable?): TcpClientSocketChannel? {
        val s = get().accept()
        if (s != null && address != null) {
            address._native = s.remoteAddress as InetSocketAddress
        }
        return s?.let { TcpClientSocketChannel(it) }
    }

    internal var bindPort: Int? = null
    actual fun bind(address: NetworkAddress) {
        try {
            val _native = address._native
            require(_native != null)
            bindPort = get().bind(_native).socket().localPort
        } catch (e: JBindException) {
            throw BindException("Address already in use: ${address.host}:${address.port}")
        }
    }

    override fun close() {
        get().close()
    }

    actual val port: Int?
        get() = bindPort

    internal var blocking = false
    actual fun setBlocking(value: Boolean) {
        native?.configureBlocking(value)
        this.blocking = value
    }

    actual fun bind(fileName: String) {
        bindUnixSocket(fileName)
    }
}
