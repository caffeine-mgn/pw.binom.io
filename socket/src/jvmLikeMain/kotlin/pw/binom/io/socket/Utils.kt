@file:JvmName("JvmUtils")

package pw.binom.io.socket

fun NetworkAddress.toJvmAddress() = if (this is JvmMutableNetworkAddress) {
    this
} else {
    JvmMutableNetworkAddress(this)
}
