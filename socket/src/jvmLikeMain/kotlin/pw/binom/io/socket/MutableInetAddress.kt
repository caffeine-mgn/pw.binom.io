package pw.binom.io.socket

import java.net.InetAddress as JvmInetAddress

actual class MutableInetAddress : InetAddress(JvmInetAddress.getLocalHost()) {
  actual fun toImmutable(): InetAddress = InetAddress(native)
  actual fun set(address: InetAddress) {
    native = address.native
  }
}
