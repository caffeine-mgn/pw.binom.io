package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.internal_copy_addrinfo
import platform.common.internal_ipv4_to_ipv6
import platform.posix.*
import platform.windows.*
import platform.windows.AF_INET
import platform.windows.AF_UNSPEC
import platform.windows.IPPROTO_TCP
import platform.windows.SOCK_STREAM

class MingwMutableNetworkAddress() : AbstractMutableNetworkAddress() {
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
        memScoped {
            init_sockets()

            val hints = alloc<addrinfo>()
            memset(hints.ptr, 0, sizeOf<addrinfo>().convert())
            hints.ai_flags = AI_CANONNAME
            hints.ai_family = AF_UNSPEC
            hints.ai_socktype = SOCK_STREAM
            hints.ai_protocol = IPPROTO_TCP

            val result = allocPointerTo<addrinfo>()
            set_posix_errno(0)
            LOOP@ while (true) {
                val err = getaddrinfo(host, port.toString(), hints.ptr, result.ptr)
                if (err == EAI_AGAIN && errno == 11 || err == EAI_NONAME) {
                    throw UnknownHostException(host)
                }
                when (err) {
                    0 -> break@LOOP
                    else -> throw RuntimeException("Unknown error: $err")
                }
            }

            data.usePinned { dataPinned ->
                val sizePtr = alloc<IntVar>()
                internal_copy_addrinfo(result.value!!, dataPinned.addressOf(0), sizePtr.ptr)
                size = sizePtr.value
                freeaddrinfo(result.value)
            }
        }
        refreshHashCode(
            host = host,
            port = port,
        )
    }

    fun <T> getAsIpV6(func: (CPointer<sockaddr_in6>) -> T): T = memScoped {
        val addrPtr = data.refTo(0).getPointer(this).reinterpret<sockaddr_in>()
        val family = addrPtr.pointed.sin_family.toInt()
        if (family == AF_INET6) {
            return@memScoped func(addrPtr.reinterpret())
        }

        val vvv = addrPtr.pointed.sin_addr
        val out = alloc<sockaddr_in6>()
        out.sin6_family = AF_INET6.convert()
        out.sin6_port = addrPtr.pointed.sin_port
        internal_ipv4_to_ipv6(vvv.ptr, out.sin6_addr.ptr, 0)
        return@memScoped func(out.ptr)
    }

    override fun clone(): MutableNetworkAddress {
        val ret = MingwMutableNetworkAddress()
        data.copyInto(ret.data)
        return ret
    }

    override val host: String
        get() = addr {
            val addr = it.reinterpret<sockaddr_in>()
            val family = addr.pointed.sin_family.toInt()
            val isV4 = family == AF_INET
            return if (isV4) {
                val ptr2 = addr.pointed.sin_addr
                val ptr3 = ptr2.reinterpret<ByteVar>().ptr
                "${ptr3[0].toUByte()}.${ptr3[1].toUByte()}.${ptr3[2].toUByte()}.${ptr3[3].toUByte()}"
            } else {
                val addr6 = it.reinterpret<sockaddr_in6>()
                val ptr2 = addr6.pointed.sin6_addr
                val ptr3 = ptr2.reinterpret<ByteVar>().ptr
                val sb = StringBuilder(16 * 2 + 8)
                repeat(16) {
                    if (it > 0 && it % 2 == 0) {
                        sb.append(":")
                    }
                    sb.append(ptr3[it].toUByte().toString(16))
                }
                sb.toString()
            }
        }
    override val port: Int
        get() = addr {
            ntohs(it.reinterpret<sockaddr_in>().pointed.sin_port).convert()
        }

    override fun toMutable(dest: MutableNetworkAddress): MutableNetworkAddress {
        TODO("Not yet implemented")
    }

    override fun toImmutable(): NetworkAddress = MingwMutableNetworkAddress(this)
}
