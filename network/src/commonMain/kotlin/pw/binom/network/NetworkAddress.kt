package pw.binom.network

import pw.binom.net.URI

expect sealed class NetworkAddress {
    val host: String
    val port: Int
    val type: Type

    enum class Type {
        IPV4,
        IPV6
    }

    class Mutable() : NetworkAddress {
        fun reset(host: String, port: Int)
        fun clone(): Mutable
    }

    class Immutable(host: String = "0.0.0.0", port: Int = 0) : NetworkAddress

    abstract fun toImmutable(): Immutable
    abstract fun toMutable(): Mutable
    abstract fun toMutable(address: Mutable)
}

fun URI.toNetworkAddress(defaultPort: Int) =
    NetworkAddress.Immutable(
        host = host,
        port = port ?: defaultPort
    )