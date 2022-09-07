package pw.binom.network

import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import kotlin.io.path.Path

private fun TcpClientSocketChannel.internalGetUnixSocket(): SocketChannel {
    var native = native
    if (native == null) {
        native = SocketChannel.open(StandardProtocolFamily.UNIX)
        native.configureBlocking(blocking)
        native.socket().tcpNoDelay = true
        this.native = native
        key?.setNative(native)
        return native
    }
    return native
}

internal actual fun TcpClientSocketChannel.internalConnectToUnixSocket(fileName: String) {
    internalGetUnixSocket().connect(UnixDomainSocketAddress.of(Path(fileName)))
}

private fun TcpServerSocketChannel.internalGetUnixSocket(): ServerSocketChannel {
    var native = native
    if (native == null) {
        native = ServerSocketChannel.open(StandardProtocolFamily.UNIX)
        native.configureBlocking(blocking)
        this.native = native
        key?.setNative(native)
        return native
    }
    return native
}

internal actual fun TcpServerSocketChannel.bindUnixSocket(fileName: String) {
    try {
        val path = Path(fileName)
        Files.deleteIfExists(path)
        internalGetUnixSocket().bind(UnixDomainSocketAddress.of(path)) // .socket().localPort
        bindPort = 0
    } catch (e: java.net.BindException) {
        throw BindException("Address already in use: \"$fileName\"")
    }
}
