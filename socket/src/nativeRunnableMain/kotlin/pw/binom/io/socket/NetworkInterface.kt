package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.socket.*
import platform.socket.internal_freeNetworkInterfaces
import platform.socket.internal_getNetworkInterfaces

@OptIn(ExperimentalForeignApi::class)
private val CPointer<NInetSocketNetworkAddress>.host: String
  get() = memScoped {
    val str = allocArray<ByteVar>(50)
    NInetSocketNetworkAddress_getHostString(this@host, str, 50)
//    internal_address_host_to_string(this@host.pointed.data, str, 50)
    str.toKString()
  }

@OptIn(ExperimentalForeignApi::class)
internal actual fun getAvailableNetworkInterfaces(): List<NetworkInterface> {
  val ret = ArrayList<NetworkInterface>()
  var inf = internal_getNetworkInterfaces()
  try {
    while (inf != null) {
      ret += NativeNetworkInterface(
        ip = inf.pointed.address.copy(),
        name = inf.pointed.name?.toKString() ?: TODO("Name is null"),
        prefixLength = inf.pointed.prefixLength,
        index = inf.pointed.index,
      )
      inf = inf.pointed.next
    }
  } finally {
    internal_freeNetworkInterfaces(inf)
  }
  return ret
}

data class NativeNetworkInterface(
  override val ip: InetAddress,
  override val name: String,
  override val prefixLength: Int,
  override val index: Int,
) : NetworkInterface
