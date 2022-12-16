package pw.binom.network

import pw.binom.url.URL

expect sealed class NetworkAddressOld {
    val host: String
    val port: Int
    val type: Type

    enum class Type {
        IPV4,
        IPV6
    }

    class Mutable() : NetworkAddressOld {
        fun reset(host: String, port: Int)
        fun clone(): Mutable
    }

    class Immutable(host: String = "0.0.0.0", port: Int = 0) : NetworkAddressOld

    abstract fun toImmutable(): Immutable
    abstract fun toMutable(): Mutable
    abstract fun toMutable(dest: Mutable)
}

fun URL.toNetworkAddress(defaultPort: Int) =
    NetworkAddressOld.Immutable(
        host = host,
        port = port ?: defaultPort
    )

/**
 * Extracts network address from @receiver url. If url not contains port throw IllegalStateException exception
 */
fun URL.toNetworkAddress() =
    NetworkAddressOld.Immutable(
        host = host,
        port = port ?: throw IllegalStateException("Url should contains port")
    )
