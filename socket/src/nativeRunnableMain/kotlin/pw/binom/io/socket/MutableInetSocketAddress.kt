package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import platform.socket.NInetSocketNetworkAddress_setHost
import platform.socket.NInetSocketNetworkAddress_setPort
import pw.binom.io.IOException

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MutableInetSocketAddress actual constructor() : InetSocketAddress() {
  actual companion object;

  actual fun setAddress(address: InetAddress) {
    address.native.use { addrPtr ->
      native.use { ptr ->
        if (NInetSocketNetworkAddress_setHost(ptr, addrPtr) != 1) {
          throw IOException("Can't set host to $address")
        }
      }
    }
  }

  actual fun setPort(port: Int) {
    native.use { ptr ->
      if (NInetSocketNetworkAddress_setPort(ptr, port) != 1) {
        throw IOException("Can't set port to $port")
      }
    }
  }

  actual fun set(address: InetAddress, port: Int) {
    address.native.use { addrPtr ->
      native.use { ptr ->
        if (NInetSocketNetworkAddress_setHost(ptr, addrPtr) != 1) {
          throw IOException("Can't set host to $address")
        }
        if (NInetSocketNetworkAddress_setPort(ptr, port) != 1) {
          throw IOException("Can't set port to $port")
        }
      }
    }
  }

  override fun toString(): String = "$host:$port"
  actual fun toImmutable(): InetSocketAddress {
    val r = InetSocketAddress()
    native.copyInto(r.native)
    return r
  }
}
