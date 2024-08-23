package pw.binom.io.socket

import pw.binom.io.ByteBuffer

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect open class TcpClientNetSocket : TcpClientSocket, NetSocket {
  constructor()
  fun connect(address: InetSocketAddress): ConnectStatus
  override fun close()
  override var blocking: Boolean
  override val id: String
  override val port: Int?
  override val tcpNoDelay: Boolean
  override fun receive(data: ByteBuffer): Int
  override fun send(data: ByteBuffer): Int
  override fun setTcpNoDelay(value: Boolean): Boolean
}
