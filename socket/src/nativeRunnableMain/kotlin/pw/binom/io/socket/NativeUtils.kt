package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.socket.*

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
fun NNetworkAddress.copy(): InetAddress {
  val result = InetAddress()
  result.native.use { resultPtr ->
    memcpy(resultPtr, ptr, sizeOf<NNetworkAddress>().convert())
  }
  return result
}
