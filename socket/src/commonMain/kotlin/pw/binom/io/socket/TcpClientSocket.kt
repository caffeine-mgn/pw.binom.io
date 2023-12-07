package pw.binom.io.socket

import pw.binom.io.ByteBuffer

interface TcpClientSocket : TcpSocket {
  fun send(data: ByteBuffer): Int
  fun receive(data: ByteBuffer): Int
}
