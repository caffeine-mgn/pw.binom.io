package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.socket.*
import pw.binom.io.InHeap

@OptIn(ExperimentalForeignApi::class)
actual open class InetSocketAddress : SocketAddress {
  actual companion object {
    actual fun resolveOrNull(host: String, port: Int): InetSocketAddress? =
      InetAddress.resolveOrNull(host)?.withPort(port)

    actual fun resolve(host: String, port: Int): InetSocketAddress =
      InetAddress.resolve(host).withPort(port)

    actual fun resolveAll(
      host: String,
      port: Int,
    ): List<InetSocketAddress> = InetAddress.resolveAll(host).map { it.withPort(port) }
  }

  actual val address: ByteArray
    get() = native.use { ptr ->
      val buffer = when (protocolFamily) {
        ProtocolFamily.AF_INET -> ByteArray(4)
        ProtocolFamily.AF_INET6 -> ByteArray(16)
        else -> return ByteArray(0)
      }
      buffer.usePinned { pinned ->
        NInetSocketNetworkAddress_getAddressBytes(ptr, pinned.addressOf(0))
      }
      buffer
    }
  actual val protocolFamily: ProtocolFamily
    get() = native.use { ptr ->
      when (NInetSocketNetworkAddress_getFamily(ptr)) {
        NET_TYPE_INET4 -> ProtocolFamily.AF_INET
        NET_TYPE_INET6 -> ProtocolFamily.AF_INET6
        NET_TYPE_UNIX -> ProtocolFamily.AF_UNIX
        else -> ProtocolFamily.AF_INET
      }
    }
  actual val isMulticastAddress: Boolean
    get() = native.use { ptr ->
      NInetSocketNetworkAddress_isMulticast(ptr) == 1
    }

  val native: InHeap<NInetSocketNetworkAddress> = InHeap.create<NInetSocketNetworkAddress>()
  override val host: String
    get() = memScoped {
      native.use { ptr ->
        val buf = allocArray<ByteVar>(50)
        NInetSocketNetworkAddress_getHostString(ptr, buf, 50)
        buf.toKString()
      }
    }
  override val port: Int
    get() = native.use { ptr ->
      NInetSocketNetworkAddress_getPort(ptr)
    }

/*  fun <T> getAsIpV6(func: (CPointer<internal_sockaddr_in6>) -> T): T = native.use { ptr ->
    when (NInetSocketNetworkAddress_getFamily(ptr)) {
      NET_TYPE_INET6 -> func(ptr.reinterpret())
      NET_TYPE_INET4 -> {
        val new = NInetSocketNetworkAddress_malloc()!!
        try {
          NInetSocketNetworkAddress_copy(ptr, new)
          NInetSocketNetworkAddress_convertToIpv6(new)
          func(new.pointed.data.reinterpret())
        } finally {
          NInetSocketNetworkAddress_free(new)
        }
      }

      else -> throw IllegalStateException("Unknown protocol")
    }
  }*/

  actual fun toMutable(): MutableInetSocketAddress {
    val result = MutableInetSocketAddress()
    toMutable(result)
    return result
  }

  actual fun toMutable(dest: MutableInetSocketAddress): MutableInetSocketAddress {
    native.copyInto(dest.native)
    return dest
  }

  override fun resolve(): InetSocketAddress = this
  override fun resolveAll(): List<InetSocketAddress> = listOf(this)
  override fun resolveOrNull(): InetSocketAddress? = this

  override fun toString(): String = "$host:$port"
}
