package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import platform.common.*
import platform.socket.*
import pw.binom.io.IOException

//@OptIn(ExperimentalForeignApi::class)
//fun bindUnixSocket(native: RawSocket, fileName: String): BindStatus =
//  when (NSocket_bindUnix(native, fileName)) {
//    BIND_RESULT_OK -> BindStatus.OK
//    BIND_RESULT_ALREADY_BINDED -> BindStatus.ALREADY_BINDED
//    BIND_RESULT_ADDRESS_ALREADY_IN_USE -> BindStatus.ADDRESS_ALREADY_IN_USE
//    BIND_RESULT_UNKNOWN_ERROR -> BindStatus.UNKNOWN
//    BIND_RESULT_NOT_SUPPORTED -> throwUnixSocketNotSupported()
//    else -> BindStatus.UNKNOWN
//  }

/*
@OptIn(ExperimentalForeignApi::class)
fun unbind(native: RawSocket) {
  internal_unbind(native)
}
*/

@OptIn(ExperimentalForeignApi::class)
fun setBlocking(native: Int, value: Boolean) {
  if (internal_set_socket_blocked_mode(native, if (value) 1 else 0) <= 0) {
    throw IOException("Can't change blocking mode. error: #${getInternalError()}")
  }
}

/*
@OptIn(ExperimentalForeignApi::class)
fun allowIpv4(native: RawSocket) {
  if (internal_set_allow_ipv6(native) <= 0) {
    throw IOException("Can't allow ipv6 connection socket. error: #$errno")
  }
}
*/

@OptIn(ExperimentalForeignApi::class)
internal fun networkErrorProcessing(msg:String?) {
  when (val er = err_getLastNetworkError()) {
    INVALID_ADDRESS_BY_PROTOCOL -> throw IOException("${msg?.let { "$it: " }?:""}Address family not supported by protocol")
    ADDRESS_NOT_AVAILABLE -> throw IOException("${msg?.let { "$it: " }?:""}Cannot assign requested address")
    0 -> return
    else -> throw IOException("${msg?.let { "$it: " }?:""}Unknown error: $er")
  }
}

//expect fun internalAccept(native: RawSocket, address: MutableInetSocketAddress?): RawSocket?
//
//expect fun internalReceive(native: RawSocket, data: ByteBuffer, address: MutableInetSocketAddress?): Int
//
//internal expect fun createSocket(socket: RawSocket, server: Boolean): Socket
//
//internal actual fun createNetworkAddress(host: String, port: Int): InetSocketAddress {
//  val ret = createMutableNetworkAddress()
//  ret.update(
//    host = host,
//    port = port,
//  )
//  return ret
//}
//
//internal actual fun createMutableNetworkAddress(): MutableInetSocketAddress =
//  CommonMutableInetNetworkSocketAddress()
