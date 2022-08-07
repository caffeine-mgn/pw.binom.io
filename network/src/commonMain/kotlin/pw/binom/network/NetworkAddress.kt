package pw.binom.network

import pw.binom.net.URL

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
    abstract fun toMutable(dest: Mutable)
}

fun URL.toNetworkAddress(defaultPort: Int) =
    NetworkAddress.Immutable(
        host = host,
        port = port ?: defaultPort
    )

/**
 * Extracts network address from @receiver url. If url not contains port throw IllegalStateException exception
 */
fun URL.toNetworkAddress() =
    NetworkAddress.Immutable(
        host = host,
        port = port ?: throw IllegalStateException("Url should contains port")
    )
