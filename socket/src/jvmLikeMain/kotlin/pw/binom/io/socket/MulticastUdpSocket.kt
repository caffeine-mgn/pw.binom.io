package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import java.net.StandardProtocolFamily
import java.net.StandardSocketOptions
import java.nio.channels.DatagramChannel
import kotlin.time.Duration
import java.net.InetSocketAddress as JvmInetSocketAddress

actual class MulticastUdpSocket(override val native: DatagramChannel) : UdpSocket, NetSocket {
  actual constructor(
    networkInterface: NetworkInterface,
    port: Int,
  ) : this(
    DatagramChannel.open(
      when (val pf = networkInterface.ip.protocolFamily) {
        ProtocolFamily.AF_INET -> StandardProtocolFamily.INET
        ProtocolFamily.AF_INET6 -> StandardProtocolFamily.INET6
        else -> throw IllegalArgumentException("Unsupported ProtocolFamily $pf")
      }
    )
  ) {
    native.setOption(StandardSocketOptions.SO_REUSEADDR, true)
    native.setOption(StandardSocketOptions.IP_MULTICAST_IF, java.net.NetworkInterface.getByName(networkInterface.name))
    native.bind(JvmInetSocketAddress(port))
  }

  actual fun send(data: ByteBuffer, address: InetSocketAddress): Int {
    try {
      return native.send(data.native, address.native)
    } catch (e: java.net.SocketException) {
      throw IOException(e.message)
    }
  }

  actual fun receive(data: ByteBuffer, address: MutableInetSocketAddress?): Int {
    val before = data.position
    if (before == data.remaining) {
      return 0
    }
    val remoteAddress = native.receive(data.native)
    if (remoteAddress != null && address != null) {
      remoteAddress as JvmInetSocketAddress
      address.native = remoteAddress
    }
    return data.position - before
  }

  actual fun setTtl(value: UByte) {
    native.socket().setOption(StandardSocketOptions.IP_MULTICAST_TTL, value.toInt())
  }

  actual fun joinGroup(address: InetAddress) {
    TODO()
  }

  actual fun joinGroup(address: InetSocketAddress, netIf: NetworkInterface) {
    native.join(
      address.native.address,
      java.net.NetworkInterface.getByName(netIf.name)
    )
  }

  actual fun leaveGroup(address: InetAddress) {
    TODO()
  }

  actual fun leaveGroup(address: InetSocketAddress, netIf: NetworkInterface) {
    TODO()
  }

  actual override fun close() {
    native.close()
  }

  actual override fun setSoTimeout(duration: Duration) {
    TODO("Not yet implemented")
  }

  actual override var blocking: Boolean
    get() = native.isBlocking
    set(value) {
      native.configureBlocking(value)
    }
  actual override val id: String
    get() = TODO("Not yet implemented")
  actual override val tcpNoDelay: Boolean
    get() = TODO("Not yet implemented")

  actual override fun setTcpNoDelay(value: Boolean): Boolean {
    TODO("Not yet implemented")
  }

  actual override val port: Int?
    get() = TODO("Not yet implemented")
}
