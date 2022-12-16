package pw.binom.io.socket

interface NetworkAddress {
    companion object {
        fun create(host: String, port: Int): NetworkAddress = createNetworkAddress(
            host = host,
            port = port,
        )
    }

    val host: String
    val port: Int
    fun toMutable(dest: MutableNetworkAddress): MutableNetworkAddress
    fun toMutable(): MutableNetworkAddress
    fun clone(): NetworkAddress
    fun toImmutable(): NetworkAddress = this
}
