package pw.binom.io.socket

import pw.binom.io.ByteBuffer

expect class MulticastUdpSocket(networkInterface: NetworkInterface, port: Int) : UdpSocket, NetSocket {
  fun send(data: ByteBuffer, address: InetSocketAddress): Int
  fun receive(data: ByteBuffer, address: MutableInetSocketAddress?): Int
  fun setTtl(value: UByte)
  fun joinGroup(address: InetAddress)
  fun joinGroup(address: InetSocketAddress, netIf: NetworkInterface)
  fun leaveGroup(address: InetAddress)
  fun leaveGroup(address: InetSocketAddress, netIf: NetworkInterface)
}
