@file:JvmName("JvmUtils")

package pw.binom.io.socket

import java.nio.channels.CancelledKeyException
import java.nio.channels.SelectionKey

//fun InetNetworkSocketAddress.toJvmAddress() = if (this is JvmMutableInetNetworkSocketAddress) {
//    this
//} else {
//    JvmMutableInetNetworkSocketAddress(this)
//}

internal fun SelectionKey.toCommonReadFlag(): ListenFlags {
  if (!isValid) {
    return ListenFlags.ERROR + ListenFlags.READ
  }
  var r = ListenFlags()
  try {
    if (isAcceptable || isReadable || isConnectable) {
      r = r.withRead
    }
    if (isWritable || isConnectable) {
      r = r.withWrite
    }
  } catch (e: CancelledKeyException) {
    r = r.withRead.withError
  }
  return r
}
