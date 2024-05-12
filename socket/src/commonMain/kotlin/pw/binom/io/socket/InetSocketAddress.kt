package pw.binom.io.socket

expect open class InetSocketAddress : SocketAddress {
  companion object {
    fun resolveOrNull(host: String, port: Int): InetSocketAddress?
    fun resolve(host: String, port: Int): InetSocketAddress
    fun resolveAll(host: String, port: Int): List<InetSocketAddress>
  }

  fun toMutable(): MutableInetSocketAddress
  val address: ByteArray
  val protocolFamily: ProtocolFamily
  val isMulticastAddress: Boolean
}
