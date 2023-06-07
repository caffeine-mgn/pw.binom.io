package pw.binom.network
/*
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlin.experimental.and

private inline infix fun Byte.ushr(other: Byte): Byte = (this.toInt() ushr other.toInt()).toByte()
private inline infix fun Byte.shr(other: Byte): Byte = (this.toInt() shr other.toInt()).toByte()

private fun calc(c: Byte): Int {
    var b = c
    var r = 0
    for (i in 0 until Byte.SIZE_BITS) {
        if ((b and 1) == 0.toByte()) {
            break
        }
        b = b shr 1
        r++
    }
    return r
}

private fun calc(c: CArrayPointer<ByteVar>, size: Int): Int {
    var r = 0
    for (i in 0 until size) {
        val b = calc(c[0])
        r += b
        if (b != Byte.SIZE_BITS) {
            break
        }
    }
    return r
}

// private fun calcNetworkPrefix(c: CPointer<sockaddr>) = when (val family = c.pointed.sa_family.toInt()) {
//    AF_INET -> {
//        calc(c.pointed.sa_data, 4)
//    }
//
//    AF_INET6 -> {
//        calc(c.pointed.sa_data, 16)
//    }
//
//    AF_PACKET -> {
//        calc(c.pointed.sa_data, 20)
//    }
//
//    else -> throw IllegalArgumentException("Unknown family $family")
// }

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
            get() = memScoped {
                TODO()
//                val dwRetVal = GetInterfaceInfo(NULL, &ulOutBufLen);
//
//                val ptrIfaddrs = allocPointerTo<ifaddrs>()
//                val result = getifaddrs(ptrIfaddrs.ptr)
//                if (result != 0) {
//                    throw IOException("Can't get local addresses: ${strerror(errno)?.toKString()}")
//                }
//                try {
//                    var addressPtr = ptrIfaddrs.value
//                    val addresses = HashMap<String, ArrayList<NetworkInterfaceAddress>>()
//                    while (addressPtr != null) {
//                        when (addressPtr.pointed.ifa_addr?.pointed?.sa_family!!.toInt()) {
//                            AF_INET, AF_INET6 -> {
//                                val list = addresses.getOrPut(addressPtr.pointed.ifa_name!!.toKString()) { ArrayList() }
//                                list += NetworkInterfaceAddress(
//                                    address = NetworkAddress.Immutable(addressPtr.pointed.ifa_addr!!),
//                                    networkPrefixAddress = calcNetworkPrefix(addressPtr.pointed.ifa_netmask!!)
//                                )
//                            }
//                        }
//                        addressPtr = addressPtr.pointed.ifa_next
//                    }
//
//                    val resultList = ArrayList<NetworkInterface>()
//                    addressPtr = ptrIfaddrs.value
//                    while (addressPtr != null) {
//                        if (addressPtr.pointed.ifa_addr?.pointed?.sa_family?.toInt() != AF_PACKET) {
//                            addressPtr = addressPtr.pointed.ifa_next
//                            continue
//                        }
//
//                        /**
//                         * Interface is running.
//                         */
//                        val isUp = (addressPtr.pointed.ifa_flags and IFF_UP.convert()).toInt() != 0
//
//                        /**
//                         * Interface is a loopback interface
//                         */
//                        val isLoopback = (addressPtr.pointed.ifa_flags and IFF_LOOPBACK.convert()).toInt() != 0
//
//                        /**
//                         * Interface is a point-to-point link
//                         */
//                        val isPointToPoint = (addressPtr.pointed.ifa_flags and IFF_POINTOPOINT.convert()).toInt() != 0
//
//                        /**
//                         * Supports multicast
//                         */
//                        val isSupportsMulticast =
//                            (addressPtr.pointed.ifa_flags and IFF_MULTICAST.convert()).toInt() != 0
//                        val name = addressPtr.pointed.ifa_name!!.toKString()
//
//                        val hardwareAddress = ByteArray(20)
//                        hardwareAddress.usePinned { p ->
//                            memcpy(
//                                p.addressOf(0),
//                                addressPtr!!.pointed.ifa_addr!!.pointed.reinterpret<sockaddr_ll>().sll_addr,
//                                20
//                            )
//                        }
//
//                        resultList += NetworkInterface(
//                            name = name,
//                            isLoopback = isLoopback,
//                            isPointToPoint = isPointToPoint,
//                            isUp = isUp,
//                            isSupportsMulticast = isSupportsMulticast,
//                            hardwareAddress = hardwareAddress,
//                            addresses = addresses[name] ?: emptyList()
//                        )
//                        addressPtr = addressPtr.pointed.ifa_next
//                    }
//                    return resultList
//                } finally {
//                    freeifaddrs(ptrIfaddrs.value)
//                }
            }
    }

    override fun toString(): String {
        return "NetworkInterface(name='$name', isLoopback=$isLoopback, isPointToPoint=$isPointToPoint, isUp=$isUp, isSupportsMulticast=$isSupportsMulticast, hardwareAddress=${hardwareAddress?.contentToString()}, addresses=$addresses)"
    }
}
*/
