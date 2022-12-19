package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.file.Files
import kotlin.io.path.Path

actual fun createTcpClientUnixSocket(): SocketChannel {
    val socket = try {
        SocketChannel.open(StandardProtocolFamily.UNIX)
    } catch (e: UnsupportedOperationException) {
        throwUnixSocketNotSupported()
    }
    runCatching { socket.socket().tcpNoDelay = true }
    return socket
}

actual fun createUdpUnixSocket(): DatagramChannel {
    val socket = try {
        DatagramChannel.open(StandardProtocolFamily.UNIX)
    } catch (e: UnsupportedOperationException) {
        throwUnixSocketNotSupported()
    }
    return socket
}

actual fun createTcpServerUnixSocket(): ServerSocketChannel {
    return try {
        ServerSocketChannel.open(StandardProtocolFamily.UNIX)
    } catch (e: UnsupportedOperationException) {
        throwUnixSocketNotSupported()
    }
}

actual fun SocketChannel.connectUnix(path: String) = connect(UnixDomainSocketAddress.of(Path(path)))

actual fun ServerSocketChannel.bindUnix(path: String) {
    val address = Path(path)
    Files.deleteIfExists(address)
    bind(UnixDomainSocketAddress.of(address))
}

actual fun DatagramChannel.sendUnix(path: String, data: ByteBuffer): Int =
    send(data.native, UnixDomainSocketAddress.of(Path(path)))

actual fun DatagramChannel.bindUnix(path: String) {
    bind(UnixDomainSocketAddress.of(Path(path)))
}
