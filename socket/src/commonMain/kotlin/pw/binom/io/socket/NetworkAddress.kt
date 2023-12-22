package pw.binom.io.socket

interface NetworkAddress {
  companion object {
    fun create(host: String, port: Int): NetworkAddress {
      require(host.isNotBlank()) { "Host is blank" }
      require(port > 0) { "Port should be greater than 0" }
      return MutableNetworkAddressImpl(host = host, port = port)
    }
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
