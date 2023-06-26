@file:JvmName("JvmUtils")

package pw.binom.io.socket

import java.nio.channels.SelectionKey

fun InetNetworkAddress.toJvmAddress() = if (this is JvmMutableInetNetworkAddress) {
    this
} else {
    JvmMutableInetNetworkAddress(this)
}

internal fun SelectionKey.toCommonReadFlag(): Int {
    var r = 0
    if (isAcceptable || isReadable || isConnectable) {
        r = r or KeyListenFlags.READ
    }
    if (isWritable || isConnectable) {
        r = r or KeyListenFlags.WRITE
    }
    return r
}
