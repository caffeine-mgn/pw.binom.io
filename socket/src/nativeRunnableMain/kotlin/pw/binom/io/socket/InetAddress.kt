@file:OptIn(UnsafeNumber::class)

package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.socket.*
import pw.binom.io.IOException
import pw.binom.io.InHeap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalForeignApi::class)
actual open class InetAddress : NetworkAddress {
  actual companion object {
    actual fun resolveOrNull(host: String): InetAddress? = memScoped {
      val size = alloc<IntVar>()
      val first = NNetworkAddressList_getAll(host, size.ptr)
      if (size.value == 0) {
        return@memScoped null
      }
      val current = InetAddress()
      current.native.use { ptr ->
        memcpy(ptr, first!!.pointed.address.ptr, sizeOf<NNetworkAddress>().convert())
      }
      NNetworkAddressList_free(first)
      current
    }

    actual fun resolveAll(host: String) = memScoped {
      val size = alloc<IntVar>()
      val first = NNetworkAddressList_getAll(host, size.ptr)
      var last = first
      if (size.value == 0) {
        return@memScoped emptyList()
      }
      val list = ArrayList<InetAddress>(size.value)
      while (last != null) {
        val current = InetAddress()
        current.native.use { ptr ->
          memcpy(ptr, last!!.pointed.address.ptr, sizeOf<NNetworkAddress>().convert())
        }
        list += current
        last = last.pointed.next
      }
      NNetworkAddressList_free(first)
      list
    }

    actual fun create(address: InetSocketAddress): InetAddress {
      val result = InetAddress()
      address.native.use { ptr ->
        result.native.use { resultPtr ->
          if (NInetSocketNetworkAddress_getHost(ptr, resultPtr) != 1) {
            throw IOException("Can't create InetNetworkAddress")
          }
        }
      }
      return result
    }

    actual fun resolve(host: String): InetAddress =
      resolveOrNull(host) ?: throw UnknownHostException(host)
  }

  val native = InHeap.create<NNetworkAddress>()
  actual val protocolFamily: ProtocolFamily
    get() = native.use { ptr ->
      when (ptr.pointed.protocolFamily) {
        NET_TYPE_INET4 -> ProtocolFamily.AF_INET
        NET_TYPE_INET6 -> ProtocolFamily.AF_INET6
        NET_TYPE_UNIX -> ProtocolFamily.AF_UNIX
        else -> ProtocolFamily.AF_INET
      }
    }
  actual val isMulticastAddress: Boolean
    get() = native.use { ptr ->
      NNetworkAddress_isMulticast(ptr) == 1
    }
  actual override val host: String
    get() = native.use { ptr ->
      memScoped {
        val buf = allocArray<ByteVar>(50)
        if (NNetworkAddress_get_host(ptr, buf, 50) <= 0) {
          throw IOException("Can't get host")
        }
        buf.toKString()
      }
    }

  fun convertIpV6() {
    native.use { ptr ->
      NNetworkAddress_convertToIpv6(ptr)
    }
  }

  actual val address: ByteArray
    get() = native.use { ptr ->
      val buffer = when (ptr.pointed.protocolFamily) {
        NET_TYPE_INET4 -> ByteArray(4)
        NET_TYPE_INET6 -> ByteArray(16)
        else -> return ByteArray(0)
      }
      buffer.usePinned { pinned ->
        NNetworkAddress_getAddressBytes(ptr, pinned.addressOf(0))
      }
      buffer
    }

  override fun toString(): String = "InetNetworkAddress($host)"
  actual fun withPort(port: Int): InetSocketAddress {
    val result = InetSocketAddress()
    result.native.use { resultPtr ->
      native.use { addressPtr ->
        if (NInetSocketNetworkAddress_setHost(resultPtr, addressPtr) != 1) {
          throw IOException("Can't create InetNetworkSocketAddress")
        }
      }
      NInetSocketNetworkAddress_setPort(resultPtr, port)
    }
    return result
  }

  actual override fun resolve(): InetAddress? = this
  actual override fun resolveAll(): List<InetAddress> = listOf(this)
  actual fun toMutable(): MutableInetAddress {
    val result = MutableInetAddress()
    native.copyInto(result.native)
    return result
  }
}
