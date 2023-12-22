package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.*
import platform.posix.init_sockets
import platform.posix.sockaddr_in

@OptIn(ExperimentalForeignApi::class)
open class CommonMutableInetNetworkAddress() : AbstractMutableInetNetworkAddress() {
  constructor(address: InetNetworkAddress) : this(
    host = address.host,
    port = address.port,
  )

  constructor(host: String, port: Int) : this() {
    update(
      host = host,
      port = port,
    )
  }

  override fun update(host: String, port: Int) {
    init_sockets()
    val ptr = internal_find_network_address(host, port.toString()) ?: throw UnknownHostException(host)
    try {
      addr { data ->
        memScoped {
          val sizePtr = alloc<IntVar>()
          internal_copy_addrinfo(ptr.reinterpret(), data, sizePtr.ptr)
          size = sizePtr.value
        }
      }
    } finally {
      internal_free_network_addresses(ptr)
    }
    val actualSize = this.nativeData.use { it.pointed.size }
    refreshHashCode(
      host = host,
      port = port,
    )
  }

  open fun <T> getAsIpV6(func: (CPointer<internal_sockaddr_in6>) -> T): T = memScoped {
    nativeData.use { dataAddr ->
      when (val family = NativeNetworkAddress_getFamily(dataAddr)) {
        NET_TYPE_INET4 -> {
          val out = alloc<internal_sockaddr_in6>()
          val convertResult = internal_addr_ipv4_to_ipv6(dataAddr.pointed.data, out.ptr)
          check(convertResult == 1 || convertResult == 0) { "Can't convert address to ipv6" }
          func(out.ptr)
        }

        NET_TYPE_INET6 -> func(dataAddr.reinterpret())

        else -> throw IllegalStateException("Invalid address family $family")
      }
    }
  }

  override fun clone(): MutableInetNetworkAddress {
    val ret = CommonMutableInetNetworkAddress()
    nativeData.copyInto(ret.nativeData)
    return ret
  }

  override val host: String
    get() = addr {
      memScoped {
        val str = allocArray<ByteVar>(50)
        internal_address_host_to_string(it, str, 50)
        str.toKString()
      }
    }
  override val port: Int
    get() = addr {
      internal_ntohs(it.reinterpret<sockaddr_in>().pointed.sin_port).convert()
    }

  override fun toImmutable(): InetNetworkAddress = CommonMutableInetNetworkAddress(this)
}

@OptIn(ExperimentalForeignApi::class)
internal inline fun <T> MutableInetNetworkAddress?.useNativeAddress(func: (CPointer<NativeNetworkAddress>?) -> T): T {
  val nn = when (this) {
    null -> null
    is CommonMutableInetNetworkAddress -> this
    else -> CommonMutableInetNetworkAddress()
  }

  val result = if (nn == null) {
    func(null)
  } else {
    nn.nativeData.use {
      func(it)
    }
  }
  if (nn !== this && nn != null && this != null) {
    this.update(
      host = nn.host,
      port = nn.port,
    )
  }
  return result
}
