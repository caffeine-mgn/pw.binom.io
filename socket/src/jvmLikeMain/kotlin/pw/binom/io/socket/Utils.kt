@file:JvmName("JvmUtils")

package pw.binom.io.socket

import java.nio.channels.SelectionKey

//fun InetNetworkSocketAddress.toJvmAddress() = if (this is JvmMutableInetNetworkSocketAddress) {
//    this
//} else {
//    JvmMutableInetNetworkSocketAddress(this)
//}

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
