package pw.binom.network

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
        fun toImmutable(): Immutable
        fun clone(): Mutable
    }

    class Immutable(host: String, port: Int) : NetworkAddress {
        fun toMutable(): Mutable
        fun toMutable(address: Mutable)
    }
}