package pw.binom.io.socket

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class MutableInetAddress() : InetAddress{
  fun set(address: InetAddress)
  fun toImmutable(): InetAddress
}
