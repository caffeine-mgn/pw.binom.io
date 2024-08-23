package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import kotlin.time.Duration

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MulticastUdpSocket(networkInterface: NetworkInterface, port: Int) : UdpSocket, NetSocket {
  fun send(data: ByteBuffer, address: InetSocketAddress): Int
  fun receive(data: ByteBuffer, address: MutableInetSocketAddress?): Int
  fun setTtl(value: UByte)
  fun joinGroup(address: InetAddress)
  fun joinGroup(address: InetSocketAddress, netIf: NetworkInterface)
  fun leaveGroup(address: InetAddress)
  fun leaveGroup(address: InetSocketAddress, netIf: NetworkInterface)
  override fun close()
  override val port: Int?
  override val tcpNoDelay: Boolean
  override val id: String
  override var blocking: Boolean
  override fun setTcpNoDelay(value: Boolean): Boolean
  override fun setSoTimeout(duration: Duration)
}
