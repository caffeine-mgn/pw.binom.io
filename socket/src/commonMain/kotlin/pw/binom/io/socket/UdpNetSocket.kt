package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import kotlin.time.Duration

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class UdpNetSocket() : UdpSocket, NetSocket {
  fun bind(address: InetSocketAddress): BindStatus
  fun send(data: ByteBuffer, address: InetSocketAddress): Int
  fun receive(data: ByteBuffer, address: MutableInetSocketAddress?): Int
  var ttl: UByte
  override val tcpNoDelay: Boolean
  override var blocking: Boolean
  override val id: String
  override val port: Int?
  override fun close()
  override fun setTcpNoDelay(value: Boolean): Boolean
  override fun setSoTimeout(duration: Duration)
}
