package pw.binom.io.socket

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect open class InetSocketAddress : SocketAddress {
  companion object {
    fun resolveOrNull(host: String, port: Int): InetSocketAddress?
    fun resolve(host: String, port: Int): InetSocketAddress
    fun resolveAll(host: String, port: Int): List<InetSocketAddress>
  }

  fun toMutable(): MutableInetSocketAddress
  fun toMutable(dest: MutableInetSocketAddress): MutableInetSocketAddress
  val address: ByteArray
  val protocolFamily: ProtocolFamily
  val isMulticastAddress: Boolean
  override val host: String
  override val port: Int
}
