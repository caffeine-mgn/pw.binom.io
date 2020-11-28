package pw.binom.io.socket

interface NetworkAddress {
    val host: String
    val port: Int
    val type: Type

    enum class Type {
        IPV4,
        IPV6
    }

    companion object
}

fun NetworkAddress.Companion.create(port: Int): NetworkAddress =
    create("0.0.0.0", port)

fun NetworkAddress.Companion.create(host: String, port: Int): NetworkAddress {
    val out = MutableNetworkAddress()
    out.reset(host, port)
    return out
}