package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.NativeNetworkAddress
import platform.common.internal_address_host_to_string
import platform.common.internal_freeNetworkInterfaces
import platform.common.internal_getNetworkInterfaces

@OptIn(ExperimentalForeignApi::class)
private val CPointer<NativeNetworkAddress>.host: String
  get() = memScoped {
    val str = allocArray<ByteVar>(50)
    internal_address_host_to_string(this@host.pointed.data, str, 50)
    str.toKString()
  }

@OptIn(ExperimentalForeignApi::class)
internal actual fun getAvailableNetworkInterfaces(): List<NetworkInterface> {
  val ret = ArrayList<NetworkInterface>()
  var inf = internal_getNetworkInterfaces()
  try {
    while (inf != null) {
      ret += NativeNetworkInterface(
        ip = inf.pointed.address?.host ?: TODO("Host is null"),
        name = inf.pointed.name?.toKString() ?: TODO("Name is null"),
        prefixLength = inf.pointed.prefixLength,
      )
      inf = inf.pointed.next
    }
  } finally {
    internal_freeNetworkInterfaces(inf)
  }
  return ret
}

private data class NativeNetworkInterface(
  override val ip: String,
  override val name: String,
  override val prefixLength: Int,
) : NetworkInterface
