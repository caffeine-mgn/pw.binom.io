package pw.binom.network

import kotlinx.cinterop.*
import platform.posix.*
import platform.posix.AF_INET
import platform.windows.*
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
                    throw RuntimeException("Can't get address. $errno  ${GetLastError()}, family: $family")
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
            hints.ai_family = platform.posix.AF_UNSPEC
            hints.ai_socktype = platform.posix.SOCK_STREAM
            hints.ai_protocol = platform.posix.IPPROTO_TCP

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

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (this === other) return true
        if (this::class != other::class) return false

        other as NetworkAddress

        if (host != other.host) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = host.hashCode()
        result = 31 * result + port.hashCode()
        return result
    }

    actual enum class Type {
        IPV4,
        IPV6
    }

    actual class Mutable : NetworkAddress() {
        actual fun reset(host: String, port: Int) {
            this._reset(host, port)
        }

        override fun toImmutable(): Immutable {
            val immutable = Immutable()
            memcpy(immutable.data.refTo(0), data.refTo(0), data.size.convert())
            return immutable
        }

        override fun toMutable(): Mutable = clone()

        override fun toMutable(address: Mutable) {
            memcpy(address.data.refTo(0), data.refTo(0), data.size.convert())
        }

        actual fun clone(): Mutable {
            val mutable = Mutable()
            toMutable(mutable)
            return mutable
        }
    }

    actual class Immutable : NetworkAddress {
        actual constructor(host: String, port: Int) : super() {
            _reset(host, port)
        }

        override fun toString(): String = "$host:$port"

        internal constructor()

        override fun toImmutable(): Immutable = this

        override fun toMutable(): Mutable {
            val mutable = Mutable()
            memcpy(mutable.data.refTo(0), data.refTo(0), data.size.convert())
            return mutable
        }

        override fun toMutable(address: Mutable) {
            memcpy(address.data.refTo(0), data.refTo(0), data.size.convert())
        }
    }

    actual abstract fun toImmutable(): Immutable
    actual abstract fun toMutable(): Mutable
    actual abstract fun toMutable(address: Mutable)
}