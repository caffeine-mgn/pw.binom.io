package pw.binom.network

data class NetworkInterfaceAddress(
    val address: NetworkAddressOld.Immutable,
    val networkPrefixAddress: Int,
)

expect class NetworkInterface {
    val name: String
    val isLoopback: Boolean
    val isPointToPoint: Boolean
    val isUp: Boolean
    val isSupportsMulticast: Boolean
    val hardwareAddress: ByteArray?
    val addresses: List<NetworkInterfaceAddress>

    companion object {
        val interfaces: List<NetworkInterface>
    }
}
