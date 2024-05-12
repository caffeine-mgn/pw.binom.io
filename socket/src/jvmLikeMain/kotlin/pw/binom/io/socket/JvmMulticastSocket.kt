package pw.binom.io.socket
/*
import java.net.InetAddress as JvmInetAddress
import java.nio.channels.DatagramChannel
import java.nio.channels.MulticastChannel

class JvmMulticastSocket(channel: DatagramChannel) : MulticastSocket,
  AbstractJvmUdpSocket(channel) {
  val channel
    get() = native as MulticastChannel
  val socket
    get()=native.socket() as java.net.MulticastSocket

  override fun setTtl(value: Byte) {
    socket.timeToLive = value + 128
  }

  override fun joinGroup(address: InetAddress) {
    socket.joinGroup(JvmInetAddress.getByName(address.host))
  }

  override fun joinGroup(address: InetSocketAddress, netIf: NetworkInterface) {
    native.socket().joinGroup(
      address.native,
      java.net.NetworkInterface.getByName(netIf.name)
    )
  }

  override fun leaveGroup(address: InetAddress) {
    socket.leaveGroup(JvmInetAddress.getByName(address.host))
  }

  override fun leaveGroup(address: InetSocketAddress, netIf: NetworkInterface) {
    native.socket().leaveGroup(
      address.native,
      java.net.NetworkInterface.getByName(netIf.name)
    )
  }
}*/
