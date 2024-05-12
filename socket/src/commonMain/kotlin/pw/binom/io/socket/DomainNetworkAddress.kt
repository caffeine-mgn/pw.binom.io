package pw.binom.io.socket

class DomainNetworkAddress(override val host: String) : NetworkAddress {
  init {
    require(host.isNotBlank()) { "Host is blank" }
  }

  fun withPort(port: Int) = DomainSocketAddress(host = host, port = port)

  override fun resolve() = InetAddress.resolveOrNull(host)
  override fun resolveAll() = InetAddress.resolveAll(host)
}
