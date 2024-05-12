package pw.binom.io.socket

import pw.binom.io.ByteBuffer

expect class UdpNetSocket() : UdpSocket, NetSocket {
  fun bind(address: InetSocketAddress): BindStatus
  fun send(data: ByteBuffer, address: InetSocketAddress): Int
  fun receive(data: ByteBuffer, address: MutableInetSocketAddress?): Int
  var ttl: UByte
}
