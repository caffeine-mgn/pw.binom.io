package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual class MutableInetAddress : InetAddress() {
  actual fun toImmutable(): InetAddress {
    val result = InetAddress()
    native.copyInto(result.native)
    return result
  }

  actual fun set(address: InetAddress) {
    address.native.copyInto(native)
  }
}
