package pw.binom.io.socket

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MutableInetSocketAddress : InetSocketAddress {
  companion object;
  constructor()

  fun setAddress(address: InetAddress)
  fun setPort(port: Int)
  fun set(address: InetAddress, port: Int)
  fun toImmutable(): InetSocketAddress
}
