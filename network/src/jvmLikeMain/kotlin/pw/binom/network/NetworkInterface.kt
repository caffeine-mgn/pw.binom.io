package pw.binom.network
/*

import pw.binom.io.socket.NetworkAddress
import java.net.NetworkInterface as JvmNetworkInterface

actual class NetworkInterface private constructor(
    actual val name: String,
    actual val isLoopback: Boolean,
    actual val isPointToPoint: Boolean,
    actual val isUp: Boolean,
    actual val isSupportsMulticast: Boolean,
    actual val hardwareAddress: ByteArray?,
    actual val addresses: List<NetworkInterfaceAddress>,
) {

    actual companion object {
        actual val interfaces: List<NetworkInterface>
            get() = JvmNetworkInterface.getNetworkInterfaces().toList().map { networkInterface ->
                NetworkInterface(
                    name = networkInterface.name,
                    isLoopback = networkInterface.isLoopback,
                    isPointToPoint = networkInterface.isPointToPoint,
                    isUp = networkInterface.isUp,
                    isSupportsMulticast = networkInterface.supportsMulticast(),
                    hardwareAddress = networkInterface.hardwareAddress,
                    addresses = networkInterface.interfaceAddresses.map { address ->
                        NetworkInterfaceAddress(
                            address = NetworkAddress.create(host = address.address.hostAddress, port = 0),
                            networkPrefixAddress = address.networkPrefixLength.toInt(),
                        )
                    }
                )
            }
    }
}
*/
