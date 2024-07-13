package pw.binom.io.socket

data class DomainSocketAddress(override val host: String, override val port: Int) : SocketAddress {
  override fun resolve() = InetSocketAddress.resolve(host = host, port = port)
  override fun resolveOrNull() = InetSocketAddress.resolveOrNull(host = host, port = port)
  override fun resolveAll() = InetSocketAddress.resolveAll(host = host, port = port)
}
