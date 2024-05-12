package pw.binom.io.socket
/*
import kotlinx.cinterop.*
import platform.common.*
import platform.posix.in_addr
import platform.posix.memcpy
import platform.socket.*
import pw.binom.io.IOException
import pw.binom.io.InHeap

@OptIn(ExperimentalForeignApi::class)
class NativeMutableInetNetworkAddress : MutableInetNetworkAddress {
  val native = InHeap.create<NNetworkAddress>()

  override val protocolFamily: ProtocolFamily
    get() = native.use { ptr ->
      when (ptr.pointed.protocolFamily) {
        NET_TYPE_INET4 -> ProtocolFamily.AF_INET
        NET_TYPE_INET6 -> ProtocolFamily.AF_INET6
        NET_TYPE_UNIX -> ProtocolFamily.AF_UNIX
        else -> ProtocolFamily.AF_INET
      }
    }
  override val isMulticastAddress: Boolean
    get() = native.use { ptr ->
      NNetworkAddress_isMulticast(ptr) == 1
    }



  inline fun <T> useData(func: (CPointer<ByteVar>) -> T) =
    native.use { ptr ->
      func(ptr.pointed.data)
    }

  val bytes: ByteArray
    get() = native.use { ptr ->
      val buffer = when (ptr.pointed.protocolFamily) {
        NET_TYPE_INET4 -> ByteArray(4)
        NET_TYPE_INET6 -> ByteArray(16)
        else -> return ByteArray(0)
      }
      buffer.usePinned { pinned ->
        NNetworkAddress_get_bytes(ptr, pinned.addressOf(0))
      }
      buffer
    }

  override var host: String
    get() = native.use { ptr ->
      memScoped {
        val buf = allocArray<ByteVar>(50)
        if (NNetworkAddress_get_host(ptr, buf, 50) <= 0) {
          throw IOException("Can't get host")
        }
        buf.toKString()
      }
    }
    set(value) {
      native.use { ptr ->
        if (NNetworkAddress_set_host(ptr, value) <= 0) {
          throw IOException("Can't set host to $value")
        }
      }
    }
}

internal actual fun createMutableInetNetworkAddress(): MutableInetNetworkAddress =
  NativeMutableInetNetworkAddress()

internal actual fun createInetNetworkAddress(host: String): InetNetworkAddress {
  val addr = NativeMutableInetNetworkAddress()
  addr.host = host
  return addr
}

fun InetNetworkAddress.toNative(): NativeMutableInetNetworkAddress {
  if (this is NativeMutableInetNetworkAddress) {
    return this
  } else {
    val r = NativeMutableInetNetworkAddress()
    r.host = host
    return r
  }
}
*/
