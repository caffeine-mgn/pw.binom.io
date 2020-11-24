package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.*
import platform.posix.AF_INET
import platform.posix.AF_UNSPEC
import platform.posix.IPPROTO_TCP
import platform.posix.SOCK_STREAM
import platform.windows.*
import pw.binom.io.IOException
import pw.binom.io.UnknownHostException

actual class MutableNetworkAddress : NetworkAddress {
    val data = ByteArray(28)
    var size = 0
    private inline fun <T> addr(f: MemScope.(CPointer<ByteVar>) -> T): T =
        memScoped {
            this.f(data.refTo(0).getPointer(this))
        }

    override val host: String
        get() {
            return addr {
                val addr = it.reinterpret<sockaddr_in>()
                val addr6 = it.reinterpret<sockaddr_in6>()
                val family = addr.pointed.sin_family.toInt()
                val isV4 = family == AF_INET
                val buf = ByteArray(50)
                val ptr = if (isV4)
                    addr.pointed.sin_addr
                else
                    addr6.pointed.sin6_addr

                if (inet_ntop(
                        family.convert(),
                        ptr.ptr,
                        buf.refTo(0).getPointer(this),
                        50.convert()
                    ) == null
                ) {
                    throw RuntimeException("Can't get address. ${errno}  ${GetLastError()}, family: $family")
                }
                buf.toKString()
            }
        }
    override val port: Int
        get() = addr {
            ntohs(it.reinterpret<sockaddr_in>().pointed.sin_port).convert()
        }

    override val type: NetworkAddress.Type
        get() = addr {
            val family = it.reinterpret<sockaddr_in>().pointed.sin_family.toInt()
            when {
                family == AF_INET -> NetworkAddress.Type.IPV4
                family == AF_INET6 -> NetworkAddress.Type.IPV6
                else -> throw IOException("Unknown network type")
            }
        }

    actual fun reset(type: NetworkAddress.Type, host: String, port: Int) {
        addr {
            val addr = it.reinterpret<sockaddr_in>()
            val family = when (type) {
                NetworkAddress.Type.IPV4 -> AF_INET
                NetworkAddress.Type.IPV6 -> AF_INET6
            }
            size = when (type) {
                NetworkAddress.Type.IPV4 -> 16
                NetworkAddress.Type.IPV6 -> 28
            }
            addr.pointed.sin_port = htons(port.convert())
            addr.pointed.sin_family = family.convert()
            if (inet_pton(
                    family.convert(),
                    host,
                    addr.pointed.sin_addr.ptr
                ) != 1
            ) {
                throw IOException("Can't set host")
            }
        }
    }

    actual fun reset(host: String, port: Int) {
        memScoped {
            init_sockets()
            val hints = alloc<addrinfo>()
            memset(hints.ptr, 0, sizeOf<addrinfo>().convert())
            hints.ai_flags = AI_CANONNAME
            hints.ai_family = AF_UNSPEC
            hints.ai_socktype = SOCK_STREAM
            hints.ai_protocol = IPPROTO_TCP

            val result = allocPointerTo<addrinfo>()
            LOOP@ while (true) {
                val err = getaddrinfo(host, port.toString(), hints.ptr, result.ptr)
                when (err) {
                    0 -> break@LOOP
                    EAI_AGAIN -> continue@LOOP
                    platform.windows.WSAHOST_NOT_FOUND -> throw UnknownHostException(host)
                    else -> throw RuntimeException("Unknown error: ${err}")
                }
            }
            memcpy(
                data.refTo(0),
                result.value!!.pointed.ai_addr,
                result.value!!.pointed.ai_addrlen.convert()
            )
            size = result.value!!.pointed.ai_addrlen.convert()
            freeaddrinfo(result.value)
        }
    }
}