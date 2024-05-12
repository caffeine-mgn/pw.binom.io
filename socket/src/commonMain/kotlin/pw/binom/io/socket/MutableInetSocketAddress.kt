package pw.binom.io.socket

expect class MutableInetSocketAddress : InetSocketAddress {
  companion object;
  constructor()

  fun setAddress(address: InetAddress)
  fun setPort(port: Int)
  fun set(address: InetAddress, port: Int)
  fun toImmutable(): InetSocketAddress
}
