package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

actual fun createTcpClientUnixSocket(): SocketChannel {
    throwUnixSocketNotSupported()
}

actual fun createUdpUnixSocket(): DatagramChannel {
    throwUnixSocketNotSupported()
}

actual fun createTcpServerUnixSocket(): ServerSocketChannel {
    throwUnixSocketNotSupported()
}

actual fun SocketChannel.connectUnix(path: String): Boolean = throwUnixSocketNotSupported()

actual fun ServerSocketChannel.bindUnix(path: String) {
    throwUnixSocketNotSupported()
}

actual fun DatagramChannel.sendUnix(path: String, data: ByteBuffer): Int =
    throwUnixSocketNotSupported()

actual fun DatagramChannel.bindUnix(path: String) {
    throwUnixSocketNotSupported()
}
