package pw.binom.io.socket

interface SocketAddress {
  val host: String
  val port: Int

  fun resolve() = InetSocketAddress.resolve(host = host, port = port)
  fun resolveOrNull() = InetSocketAddress.resolveOrNull(host = host, port = port)
  fun resolveAll() = InetSocketAddress.resolveAll(host = host, port = port)
}
