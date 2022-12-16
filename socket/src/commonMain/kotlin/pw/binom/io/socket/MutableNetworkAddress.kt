package pw.binom.io.socket

interface MutableNetworkAddress : NetworkAddress {
    companion object {
        fun create() = createMutableNetworkAddress()
        fun create(host: String, port: Int): MutableNetworkAddress {
            val ret = create()
            ret.update(
                host = host,
                port = port
            )
            return ret
        }
    }

    fun update(host: String, port: Int)
    override fun clone(): MutableNetworkAddress
}
