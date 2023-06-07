package pw.binom.network
/*

import kotlinx.cinterop.*
import platform.common.internal_copy_address
import platform.linux.freeifaddrs
import platform.linux.getifaddrs
import platform.linux.ifaddrs
import platform.linux.sockaddr_ll
import platform.posix.*
import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.useName
import pw.binom.io.IOException
import pw.binom.io.socket.CommonMutableNetworkAddress
import kotlin.experimental.and

private inline infix fun Byte.ushr(other: Byte): Byte = (this.toInt() ushr other.toInt()).toByte()
private inline infix fun Byte.shr(other: Byte): Byte = (this.toInt() shr other.toInt()).toByte()

private fun calc(address: Byte): Int {
    var b = address
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

private fun calc(address: CArrayPointer<ByteVar>, size: Int): Int {
    var r = 0
    for (i in 0 until size) {
        val b = calc(address = address[0])
        r += b
        if (b != Byte.SIZE_BITS) {
            break
        }
    }
    return r
}

private fun calcNetworkPrefix(address: CPointer<sockaddr>) = when (val family = address.pointed.sa_family.toInt()) {
    AF_INET -> calc(address = address.pointed.sa_data, size = 4)
    AF_INET6 -> calc(address = address.pointed.sa_data, size = 16)
    AF_PACKET -> calc(address = address.pointed.sa_data, size = 20)
    else -> throw IllegalArgumentException("Unknown family $family")
}

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
                val ptrIfaddrs = allocPointerTo<ifaddrs>()
                val result = getifaddrs(ptrIfaddrs.ptr)
                if (result != 0) {
                    throw IOException("Can't get local addresses: ${strerror(errno)?.toKString()}")
                }
                try {
                    var addressPtr = ptrIfaddrs.value
                    val addresses =
                        defaultMutableMap<String, ArrayList<NetworkInterfaceAddress>>().useName("NetworkInterface")
                    while (addressPtr != null) {
                        when (addressPtr.pointed.ifa_addr?.pointed?.sa_family!!.toInt()) {
                            AF_INET, AF_INET6 -> {
                                val list = addresses.getOrPut(addressPtr.pointed.ifa_name!!.toKString()) { ArrayList() }
                                val sizePtr = alloc<IntVar>()

                                val addr = CommonMutableNetworkAddress()
                                addr.data.usePinned { dataPinned ->
                                    internal_copy_address(
                                        addressPtr!!.pointed.ifa_addr!!,
                                        dataPinned.addressOf(0),
                                        sizePtr.ptr,
                                    )
                                }
                                addr.size = sizePtr.value
                                list += NetworkInterfaceAddress(
                                    address = addr,
                                    networkPrefixAddress = calcNetworkPrefix(address = addressPtr.pointed.ifa_netmask!!)
                                )
                            }
                        }
                        addressPtr = addressPtr.pointed.ifa_next
                    }

                    val resultList = defaultMutableList<NetworkInterface>()
                    addressPtr = ptrIfaddrs.value
                    while (addressPtr != null) {
                        if (addressPtr.pointed.ifa_addr?.pointed?.sa_family?.toInt() != AF_PACKET) {
                            addressPtr = addressPtr.pointed.ifa_next
                            continue
                        }

                        */
/**
                         * Interface is running.
                         *//*

                        val isUp = (addressPtr.pointed.ifa_flags and IFF_UP.convert()).toInt() != 0

                        */
/**
                         * Interface is a loopback interface
                         *//*

                        val isLoopback = (addressPtr.pointed.ifa_flags and IFF_LOOPBACK.convert()).toInt() != 0

                        */
/**
                         * Interface is a point-to-point link
                         *//*

                        val isPointToPoint = (addressPtr.pointed.ifa_flags and IFF_POINTOPOINT.convert()).toInt() != 0

                        */
/**
                         * Supports multicast
                         *//*

                        val isSupportsMulticast =
                            (addressPtr.pointed.ifa_flags and IFF_MULTICAST.convert()).toInt() != 0
                        val name = addressPtr.pointed.ifa_name!!.toKString()

                        val hardwareAddress = ByteArray(20)
                        hardwareAddress.usePinned { p ->
                            memcpy(
                                p.addressOf(0),
                                addressPtr!!.pointed.ifa_addr!!.pointed.reinterpret<sockaddr_ll>().sll_addr,
                                20
                            )
                        }

                        resultList += NetworkInterface(
                            name = name,
                            isLoopback = isLoopback,
                            isPointToPoint = isPointToPoint,
                            isUp = isUp,
                            isSupportsMulticast = isSupportsMulticast,
                            hardwareAddress = hardwareAddress,
                            addresses = addresses[name] ?: emptyList()
                        )
                        addressPtr = addressPtr.pointed.ifa_next
                    }
                    return resultList
                } finally {
                    freeifaddrs(ptrIfaddrs.value)
                }
            }
    }

    override fun toString(): String {
        return "NetworkInterface(name='$name', isLoopback=$isLoopback, isPointToPoint=$isPointToPoint, isUp=$isUp, isSupportsMulticast=$isSupportsMulticast, hardwareAddress=${hardwareAddress?.contentToString()}, addresses=$addresses)"
    }
}
*/
