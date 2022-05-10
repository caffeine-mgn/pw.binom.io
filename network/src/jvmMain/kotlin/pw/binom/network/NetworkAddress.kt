package pw.binom.network

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.UnknownHostException as JUnknownHostException

actual sealed class NetworkAddress {

    var _native: InetSocketAddress? = null

    actual val host: String
        get() {
            val native = _native
            require(native != null)
            return native.address.hostAddress
        }

    actual val port: Int
        get() {
            val native = _native
            require(native != null)
            return native.port
        }

    actual val type: Type
        get() {
            val native = _native
            require(native != null)
            return when (native.address) {
                is Inet4Address -> Type.IPV4
                is Inet6Address -> Type.IPV6
                else -> throw IllegalStateException("Unknown network type ${native.address::class.java.name}")
            }
        }

    override fun toString(): String = "$host:$port"

    protected fun _reset(host: String, port: Int) {
        try {
            _native = InetSocketAddress(InetAddress.getByName(host), port)
        } catch (e: JUnknownHostException) {
            throw UnknownHostException(host)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NetworkAddress) return false

        if (host != other.host) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = host.hashCode()
        result = 31 * result + port
        return result
    }

    actual enum class Type {
        IPV4,
        IPV6
    }

    actual class Mutable actual constructor() : NetworkAddress() {
        actual fun reset(host: String, port: Int) {
            _reset(host, port)
        }

        override fun toImmutable(): Immutable {
            require(_native != null)
            val res = Immutable()
            res._native = _native
            return res
        }

        override fun toMutable(): Mutable = clone()

        override fun toMutable(dest: Mutable) {
            dest._native = _native
        }

        actual fun clone(): Mutable {
            val res = Mutable()
            toMutable(res)
            return res
        }
    }

    actual class Immutable : NetworkAddress {
        actual constructor(host: String, port: Int) : super() {
            _reset(host, port)
        }

        constructor() : super()

        override fun toImmutable(): Immutable = this

        override fun toMutable(): Mutable {
            val res = Mutable()
            res._native = _native
            return res
        }

        override fun toMutable(dest: Mutable) {
            dest._native = _native
        }
    }

    actual abstract fun toImmutable(): Immutable
    actual abstract fun toMutable(): Mutable
    actual abstract fun toMutable(dest: Mutable)
}
