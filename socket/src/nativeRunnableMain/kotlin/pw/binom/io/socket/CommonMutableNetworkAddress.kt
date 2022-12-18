package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.*
import platform.posix.*

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
            memScoped {
                val sizePtr = alloc<IntVar>()
                data.usePinned { dataPinned ->
                    internal_copy_addrinfo(ptr.reinterpret(), dataPinned.addressOf(0), sizePtr.ptr)
                }
                size = sizePtr.value
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
        data.usePinned { dataPinned ->
            val dataAddr = dataPinned.addressOf(0) // .getPointer(this)
            when (val family = internal_addr_get_family(dataAddr)) {
                4 -> {
                    val out = alloc<internal_sockaddr_in6>()
                    internal_addr_ipv4_to_ipv6(dataAddr, out.ptr)
                    func(out.ptr)
                }
                6 -> {
                    func(dataAddr.reinterpret())
                }

                else -> throw IllegalStateException("Invalid address family $family")
            }
        }
    }

    override fun clone(): MutableNetworkAddress {
        val ret = CommonMutableNetworkAddress()
        data.copyInto(ret.data)
        return ret
    }

    override val host: String
        get() = addr {
            val str = allocArray<ByteVar>(50)
            internal_address_host_to_string(it, str, 50)
            str.toKString()
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
