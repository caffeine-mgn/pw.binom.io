package pw.binom.io.socket

import pw.binom.io.Closeable
import java.nio.channels.DatagramChannel
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.AbstractSelectableChannel

actual interface Socket : Closeable {
    actual var blocking: Boolean
    val native: AbstractSelectableChannel

    actual companion object {
        actual fun createTcpClientNetSocket(): TcpClientNetSocket = JvmTcpClientSocket(SocketChannel.open())

        actual fun createTcpClientUnixSocket(): TcpClientUnixSocket =
            JvmTcpClientSocket(pw.binom.io.socket.createTcpClientUnixSocket())

        actual fun createUdpNetSocket(): UdpNetSocket = JvmUdpSocket(DatagramChannel.open())

        actual fun createUdpUnixSocket(): UdpUnixSocket = JvmUdpSocket(pw.binom.io.socket.createUdpUnixSocket())
        actual fun createTcpServerNetSocket(): TcpNetServerSocket = JvmTcpServerSocket(ServerSocketChannel.open())

        actual fun createTcpServerUnixSocket(): TcpUnixServerSocket =
            JvmTcpServerSocket(pw.binom.io.socket.createTcpServerUnixSocket())
    }
}
