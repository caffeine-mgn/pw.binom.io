package pw.binom.io.socket

import pw.binom.io.ByteBuffer

interface UdpUnixSocket : UdpSocket {
  fun bind(path: String): BindStatus
  fun send(data: ByteBuffer, address: String): Int
  fun receive(data: ByteBuffer, address: (String) -> Unit?): Int
}
