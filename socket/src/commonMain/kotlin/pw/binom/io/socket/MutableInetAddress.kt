package pw.binom.io.socket

expect class MutableInetAddress() : InetAddress{
  fun set(address: InetAddress)
  fun toImmutable(): InetAddress
}
