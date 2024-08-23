package pw.binom.io.socket

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class TcpUnixServerSocket() : TcpServerSocket {
  fun accept(): TcpClientUnixSocket?
  fun bind(path: String): BindStatus
  override val id: String
  override fun close()
  override var blocking: Boolean
  override val tcpNoDelay: Boolean
  override fun setTcpNoDelay(value: Boolean): Boolean
}
