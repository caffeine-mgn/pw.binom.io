package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.*
import platform.posix.*
import pw.binom.io.copyInto

open class CommonMutableNetworkAddress() : AbstractMutableNetworkAddress() {
    constructor(address: NetworkAddress) : this(
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

    override fun clone(): MutableNetworkAddress {
        val ret = CommonMutableNetworkAddress()
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

    override fun toMutable(dest: MutableNetworkAddress): MutableNetworkAddress {
        dest.update(
            host = host,
            port = port,
        )
        return dest
    }

    override fun toImmutable(): NetworkAddress = CommonMutableNetworkAddress(this)
}

internal inline fun <T> MutableNetworkAddress?.useNativeAddress(func: (CPointer<NativeNetworkAddress>?) -> T): T {
    val nn = when (this) {
        null -> null
        is CommonMutableNetworkAddress -> this
        else -> CommonMutableNetworkAddress()
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
