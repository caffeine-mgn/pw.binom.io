@file:JvmName("JvmUtils")

package pw.binom.io.socket

import java.nio.channels.SelectionKey

fun InetNetworkAddress.toJvmAddress() = if (this is JvmMutableInetNetworkAddress) {
    this
} else {
    JvmMutableInetNetworkAddress(this)
}

internal fun SelectionKey.toCommonReadFlag(): ListenFlags {
    var r = ListenFlags()
    if (isAcceptable || isReadable || isConnectable) {
        r = r.withRead
    }
    if (isWritable || isConnectable) {
        r = r.withWrite
    }
    return r
}
