package pw.binom.io.socket

internal data class MutableNetworkAddressImpl(
    override var host: String,
    override var port: Int,
) : MutableNetworkAddress {
    override fun toString(): String = "$host:$port"
}