package pw.binom.io.socket

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class TcpNetServerSocket() : TcpServerSocket, NetSocket {
  fun accept(address: MutableInetAddress? = null): TcpClientNetSocket?
  fun bind(address: InetSocketAddress): BindStatus
  override val tcpNoDelay: Boolean
  override var blocking: Boolean
  override val port: Int?
  override val id: String
  override fun close()
  override fun setTcpNoDelay(value: Boolean): Boolean
}
