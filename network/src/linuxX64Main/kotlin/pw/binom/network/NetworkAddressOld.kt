package pw.binom.network

import kotlinx.cinterop.*
import platform.common.internal_ipv4_to_ipv6
import platform.linux.inet_ntop
import platform.posix.*
import pw.binom.io.IOException

actual sealed class NetworkAddressOld {
    val data = ByteArray(28)
    var size = 0

    internal fun setAddress(sockaddr: CPointer<sockaddr>) {
        when (val family = sockaddr.pointed.sa_family.convert<Int>()) {
            AF_INET -> {
                val addr = sockaddr.reinterpret<sockaddr_in>()
                size = sizeOf<sockaddr_in>().convert()
                data.usePinned { dataPinned ->
                    memcpy(
                        dataPinned.addressOf(0),
                        addr,
                        size.convert()
                    )
                }
            }

            AF_INET6 -> {
                val addr = sockaddr.reinterpret<sockaddr_in6>()
                size = sizeOf<sockaddr_in6>().convert()
                data.usePinned { dataPinned ->
                    memcpy(
                        dataPinned.addressOf(0),
                        addr,
                        size.convert()
                    )
                }
            }

            AF_PACKET -> {
                TODO("AF_PACKET not supported")
//                val addr = sockaddr.reinterpret<sockaddr_ll>()
//                size = sizeOf<sockaddr_ll>().convert()
//                data.usePinned { dataPinned ->
//                    memcpy(
//                        dataPinned.addressOf(0),
//                        addr,
//                        size.convert()
//                    )
//                }
            }

            else -> throw IllegalArgumentException("Unknown family=$family")
        }
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (this === other) return true
        if (other !is NetworkAddressOld) return false

        if (host != other.host) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int = hashCode

    fun <T> isAddrV6(func: (CPointer<sockaddr_in6>) -> T): T {
        memScoped {
            val addrPtr = data.refTo(0).getPointer(this)
            val family = addrPtr.reinterpret<sockaddr_in>().pointed.sin_family.toInt()
            if (family == AF_INET6) {
                return func(addrPtr.reinterpret())
            }
            val vvv = addrPtr.reinterpret<sockaddr_in>().pointed.sin_addr
            val out = alloc<sockaddr_in6>()
            out.sin6_family = AF_INET6.convert()
            out.sin6_port = addrPtr.reinterpret<sockaddr_in>().pointed.sin_port
            internal_ipv4_to_ipv6(vvv.ptr, out.sin6_addr.ptr, 0)
            return func(out.ptr)
        }
    }

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
                val ptr = if (isV4) {
                    addr.pointed.sin_addr
                } else {
                    addr6.pointed.sin6_addr
                }
                set_posix_errno(0)
                ByteArray(50).usePinned { buf ->

                    if (inet_ntop(
                            family.convert(),
                            ptr.ptr,
                            buf.addressOf(0),
                            50.convert()
                        ) == null
                    ) {
                        if (errno == EAFNOSUPPORT) {
                            throw IOException("Address family not supported by protocol. type: $type")
                        }
                        throw RuntimeException("Can't get address. $errno, family: $family")
                    }
                    buf.get().toKString()
                }
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
    private var hashCode = 0
    protected fun refreshHashCode() {
        var hashCode = host.hashCode()
        hashCode = 31 * hashCode + port.hashCode()
        this.hashCode = hashCode
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
                if (err == EAI_AGAIN && errno == 11 || err == EAI_NONAME) {
                    throw UnknownHostException(host)
                }
                when (err) {
                    0 -> break@LOOP
                    else -> throw RuntimeException("Unknown error: $err")
                }
            }
            result.value!!.pointed.ai_next
            memcpy(
                data.refTo(0),
                result.value!!.pointed.ai_addr,
                result.value!!.pointed.ai_addrlen.convert()
            )
            size = result.value!!.pointed.ai_addrlen.convert()
            freeaddrinfo(result.value)
        }
        refreshHashCode()
    }

    actual enum class Type {
        IPV4,
        IPV6
    }

    actual class Mutable : NetworkAddressOld() {
        actual fun reset(host: String, port: Int) {
            this._reset(host, port)
        }

        override fun toImmutable(): Immutable {
            val immutable = Immutable()
            memcpy(immutable.data.refTo(0), data.refTo(0), data.size.convert())
            immutable.refreshHashCode()
            return immutable
        }

        override fun toMutable(): Mutable = clone()

        override fun toMutable(dest: Mutable) {
            memcpy(dest.data.refTo(0), data.refTo(0), data.size.convert())
            dest.refreshHashCode()
        }

        actual fun clone(): Mutable {
            val mutable = Mutable()
            toMutable(mutable)
            return mutable
        }
    }

    actual class Immutable : NetworkAddressOld {
        actual constructor(host: String, port: Int) : super() {
            _reset(host, port)
        }

        internal constructor()
        internal constructor(sockaddr: CPointer<sockaddr>) : this() {
            setAddress(sockaddr)
        }

        override fun toString(): String = "$host:$port"
        override fun toImmutable(): Immutable = this

        override fun toMutable(): Mutable {
            val mutable = Mutable()
            memcpy(mutable.data.refTo(0), data.refTo(0), data.size.convert())
            mutable.refreshHashCode()
            return mutable
        }

        override fun toMutable(dest: Mutable) {
            memcpy(dest.data.refTo(0), data.refTo(0), data.size.convert())
            dest.refreshHashCode()
        }
    }

    actual abstract fun toImmutable(): Immutable
    actual abstract fun toMutable(): Mutable
    actual abstract fun toMutable(dest: Mutable)
}
