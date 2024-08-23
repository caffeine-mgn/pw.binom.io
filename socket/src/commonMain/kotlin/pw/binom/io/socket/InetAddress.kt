package pw.binom.io.socket

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect open class InetAddress : NetworkAddress {
  companion object {
    fun create(address: InetSocketAddress): InetAddress
    fun resolveOrNull(host: String): InetAddress?
    fun resolve(host: String): InetAddress
    fun resolveAll(host: String): List<InetAddress>
  }

  fun withPort(port: Int): InetSocketAddress
  val address: ByteArray
  val protocolFamily: ProtocolFamily
  val isMulticastAddress: Boolean
  fun toMutable(): MutableInetAddress
  override val host: String
  override fun resolve(): InetAddress?
  override fun resolveAll(): List<InetAddress>
}
