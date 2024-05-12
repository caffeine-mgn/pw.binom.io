package pw.binom.io.socket

import pw.binom.io.Closeable
import java.net.InetSocketAddress as JvmInetSocketAddress
import java.net.StandardProtocolFamily
import java.net.StandardSocketOptions
import java.nio.channels.DatagramChannel
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.spi.AbstractSelectableChannel

actual interface Socket : Closeable {
  actual var blocking: Boolean
  val native: AbstractSelectableChannel
  actual val id: String

  actual companion object {
//    actual fun createTcpClientNetSocket(): TcpClientNetSocket = TODO()//JvmTcpClientSocket(SocketChannel.open())

//    actual fun createTcpClientUnixSocket(): TcpClientUnixSocket =
//      JvmTcpClientSocket(pw.binom.io.socket.createTcpClientUnixSocket())

//    actual fun createUdpNetSocket(): UdpNetSocket = JvmUdpSocket(DatagramChannel.open())

//    actual fun createUdpUnixSocket(): UdpUnixSocket = JvmUdpSocket(pw.binom.io.socket.createUdpUnixSocket())

//    actual fun createTcpServerNetSocket(): TcpNetServerSocket = TODO()//JvmTcpServerSocket(ServerSocketChannel.open())

//    actual fun createTcpServerUnixSocket(): TcpUnixServerSocket =
//      JvmTcpServerSocket(pw.binom.io.socket.createTcpServerUnixSocket())

/*    actual fun createMulticastSocket(port: Int, networkInterface: NetworkInterface): MulticastSocket {
      val channel = DatagramChannel.open(StandardProtocolFamily.INET) // ipv6?
        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
        .setOption(StandardSocketOptions.IP_MULTICAST_IF, java.net.NetworkInterface.getByName(networkInterface.name))
        .bind(JvmInetSocketAddress(port))
      return JvmMulticastSocket(channel)
    }*/
  }

  actual val tcpNoDelay: Boolean

  actual fun setTcpNoDelay(value: Boolean): Boolean
}
