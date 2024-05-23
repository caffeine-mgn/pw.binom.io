package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import platform.posix.errno
import platform.socket.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.InHeap

@OptIn(ExperimentalForeignApi::class)
actual open class TcpClientNetSocket(init: Boolean) : TcpClientSocket, NetSocket,AbstractTcpSocket(init) {

  actual constructor() : this(true)

  override fun initSocket() {
    data.use { ptr ->
      if (NSocket_create(ptr, NET_TYPE_INET6, SOCKET_TYPE_TCP) != 1) {
        throw IOException("Can't create socket")
      }
    }
  }

  override val port: Int?
    get() = data.use { ptr ->
      NSocket_getSocketPort(ptr).takeIf { it != -1 }
    }

  actual fun connect(address: InetSocketAddress): ConnectStatus {
    val result = data.use { ptr ->
      address.native.use { addrPtr ->
        NSocket_connectInet(ptr, addrPtr)
      }
    }
    return when (result) {
      ConnectStatus_OK -> ConnectStatus.OK
      ConnectStatus_CONNECTION_REFUSED -> ConnectStatus.CONNECTION_REFUSED
      ConnectStatus_ALREADY_CONNECTED -> ConnectStatus.ALREADY_CONNECTED
      ConnectStatus_IN_PROGRESS -> ConnectStatus.IN_PROGRESS
      else -> throw IOException("Can't connect to $address $result $errno")
    }
  }
}
