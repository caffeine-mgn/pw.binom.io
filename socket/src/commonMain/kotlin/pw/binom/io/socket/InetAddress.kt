package pw.binom.io.socket

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
}
