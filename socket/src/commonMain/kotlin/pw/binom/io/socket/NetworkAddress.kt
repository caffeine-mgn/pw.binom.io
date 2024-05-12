package pw.binom.io.socket

sealed interface NetworkAddress {
  val host: String

  fun resolve(): InetAddress? = InetAddress.resolveOrNull(host)
  fun resolveAll(): List<InetAddress> = InetAddress.resolveAll(host)
}
