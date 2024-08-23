package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
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
