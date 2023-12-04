package pw.binom.io.socket

interface MutableInetNetworkAddress : InetNetworkAddress {
  companion object {
    fun create() = createMutableNetworkAddress()
    fun create(host: String, port: Int): MutableInetNetworkAddress {
      val ret = create()
      ret.update(
        host = host,
        port = port,
      )
      return ret
    }
  }

  fun update(host: String, port: Int)
  override fun clone(): MutableInetNetworkAddress
}
