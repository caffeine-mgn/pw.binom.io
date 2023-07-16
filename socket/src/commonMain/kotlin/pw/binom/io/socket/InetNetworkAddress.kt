package pw.binom.io.socket

interface InetNetworkAddress : NetworkAddress {
  companion object {
    fun create(host: String, port: Int): InetNetworkAddress = createNetworkAddress(
      host = host,
      port = port,
    )

    fun anyIpv4(port: Int) = create(host = "0.0.0.0", port = port)
    fun anyIpv6(port: Int) = create(host = "::", port = port)
  }

  fun toMutable(dest: MutableInetNetworkAddress): MutableInetNetworkAddress
  fun toMutable(): MutableInetNetworkAddress
  override fun clone(): InetNetworkAddress
  fun toImmutable(): InetNetworkAddress = this
  override fun resolve(): InetNetworkAddress = this
}
