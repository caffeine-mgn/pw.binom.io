package pw.binom.network

import pw.binom.io.Closeable
import java.net.InetSocketAddress
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.file.Files
import kotlin.io.path.Path
import java.net.BindException as JBindException
import java.nio.channels.ServerSocketChannel as JServerSocketChannel

actual class TcpServerSocketChannel : Closeable {
    var native: JServerSocketChannel? = null
        private set
    var key: JvmSelector.JvmKey? = null
        set(value) {
            field = value
            if (native != null) {
                key?.setNative(native!!)
            }
        }

    private fun getUnixSocket(): JServerSocketChannel {
        var native = native
        if (native == null) {
            native = JServerSocketChannel.open(StandardProtocolFamily.UNIX)
            native.configureBlocking(blocking)
            this.native = native
            key?.setNative(native)
            return native
        }
        return native
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

    private var bindPort: Int? = null
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

    private var blocking = false
    actual fun setBlocking(value: Boolean) {
        native?.configureBlocking(value)
        this.blocking = value
    }

    actual fun bind(fileName: String) {
        try {
            val path = Path(fileName)
            Files.deleteIfExists(path)
            getUnixSocket().bind(UnixDomainSocketAddress.of(path)) // .socket().localPort
            bindPort = 0
        } catch (e: JBindException) {
            throw BindException("Address already in use: \"$fileName\"")
        }
    }
}
