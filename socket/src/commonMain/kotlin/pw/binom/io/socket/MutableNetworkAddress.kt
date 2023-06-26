package pw.binom.io.socket

interface MutableNetworkAddress : NetworkAddress {
    companion object {
        fun create(host: String, port: Int): MutableNetworkAddress = MutableNetworkAddressImpl(host = host, port = port)
    }

    override fun clone() = create(host = host, port = port)
    override var host: String
    override var port: Int
}