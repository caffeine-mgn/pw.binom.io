package pw.binom.io.socket

import java.net.StandardProtocolFamily
import java.nio.channels.DatagramChannel
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

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
