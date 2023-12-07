package pw.binom.io.socket

interface NetworkAddress {
  companion object {
    fun create(host: String, port: Int): NetworkAddress = MutableNetworkAddressImpl(host = host, port = port)
  }

  fun clone() = create(host = host, port = port)

  val host: String
  val port: Int

  fun resolve(): InetNetworkAddress = InetNetworkAddress.create(
    host = host,
    port = port,
  )

  fun <T : MutableInetNetworkAddress> resolve(dest: T): T {
    dest.update(
      host = host,
      port = port,
    )
    return dest
  }
}
