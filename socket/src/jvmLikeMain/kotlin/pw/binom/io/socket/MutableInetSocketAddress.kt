package pw.binom.io.socket

import java.net.InetAddress as JvmInetAddress
import java.net.InetSocketAddress as JvmInetSocketAddress

actual class MutableInetSocketAddress actual constructor() :
  InetSocketAddress(JvmInetSocketAddress(JvmInetAddress.getLocalHost(), 80)) {
  actual companion object;


  actual fun setAddress(address: InetAddress) {
    native = JvmInetSocketAddress(address.native, native.port)
  }

  actual fun setPort(port: Int) {
    native = JvmInetSocketAddress(native.address, port)
  }

  actual fun set(address: InetAddress, port: Int) {
    native = JvmInetSocketAddress(address.native, port)
  }

  actual fun toImmutable(): InetSocketAddress =
    InetSocketAddress(native)
}
