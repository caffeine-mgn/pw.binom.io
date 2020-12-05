package pw.binom.network

import kotlinx.cinterop.*
import platform.linux.inet_ntop
import platform.posix.*
import platform.posix.AF_INET
import pw.binom.io.IOException

actual sealed class NetworkAddress {
    val data = ByteArray(28)
    var size = 0

    private inline fun <T> addr(f: MemScope.(CPointer<ByteVar>) -> T): T =
        memScoped {
            this.f(data.refTo(0).getPointer(this))
        }

    actual val host: String
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
                    throw RuntimeException("Can't get address. $errno, family: $family")
                }
                buf.toKString()
            }
        }

    actual val port: Int
        get() = addr {
            ntohs(it.reinterpret<sockaddr_in>().pointed.sin_port).convert()
        }

    actual val type: Type
        get() = addr {
            val family = it.reinterpret<sockaddr_in>().pointed.sin_family.toInt()
            when (family) {
                AF_INET -> Type.IPV4
                AF_INET6 -> Type.IPV6
                else -> throw IOException("Unknown network type $family")
            }
        }

    protected fun _reset(host: String, port: Int) {
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
                if (err == EAI_AGAIN && errno == 11) {
                    throw UnknownHostException(host)
                }
                when (err) {
                    0 -> break@LOOP
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

    actual enum class Type {
        IPV4,
        IPV6
    }

    actual class Mutable : NetworkAddress() {
        actual fun reset(host: String, port: Int) {
            this._reset(host, port)
        }

        actual fun toImmutable(): Immutable {
            val immutable = Immutable()
            memcpy(immutable.data.refTo(0), data.refTo(0), data.size.convert())
            return immutable
        }

        actual fun clone(): Mutable {
            val mutable = Mutable()
            memcpy(mutable.data.refTo(0), data.refTo(0), data.size.convert())
            return mutable
        }
    }

    actual class Immutable : NetworkAddress {
        actual constructor(host: String, port: Int) : super() {
            _reset(host, port)
        }

        internal constructor()

        override fun toString(): String = "$host:$port"

        actual fun toMutable(): Mutable {
            val mutable = Mutable()
            memcpy(mutable.data.refTo(0), data.refTo(0), data.size.convert())
            return mutable
        }

        actual fun toMutable(address: Mutable) {
            memcpy(address.data.refTo(0), data.refTo(0), data.size.convert())
        }
    }
}