package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import platform.posix.errno
import platform.socket.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.InHeap

@OptIn(ExperimentalForeignApi::class)
actual open class TcpClientNetSocket(init: Boolean) : TcpClientSocket, NetSocket {
  override val data = InHeap.create<NSocket>()

  actual constructor() : this(true)

  init {
    if (init) {
      data.use { ptr ->
        if (NSocket_create(ptr, NET_TYPE_INET6, SOCKET_TYPE_TCP) != 1) {
          throw IOException("Can't create socket")
        }
      }
    }
  }

  override var blocking: Boolean
    get() = data.use { ptr ->
      NSocket_getBlockedMode(ptr) == 1
    }
    set(value) {
      data.use { ptr ->
        NSocket_setBlockedMode(ptr, if (value) 1 else 0)
      }
    }
  override val native: RawSocket = data.use { ptr -> ptr.pointed.native }
  override val server: Boolean
    get() = false
  override var keyHash: Int
    get() = TODO("Not yet implemented")
    set(value) {}
  override val id: String
    get() = TODO("Not yet implemented")
  override val tcpNoDelay: Boolean
    get() = data.use { ptr ->
      NSocket_getNoDelay(ptr) == 1
    }

  override fun close() {
    data.use { ptr ->
      NSocket_close(ptr)
    }
  }

  override fun send(data: ByteBuffer): Int {
    if (!data.isReferenceAccessAvailable()) {
      return 0
    }
    return this.data.use { ptr ->
      data.ref(0) { dataPtr, dataLen ->
        NSocket_send(ptr, dataPtr, dataLen)
      }
    }
  }

  override fun receive(data: ByteBuffer): Int {
    if (!data.isReferenceAccessAvailable()) {
      return 0
    }
    val wasRead = this.data.use { ptr ->
      data.ref(0) { dataPtr, len ->
        NSocket_receiveOnly(ptr, dataPtr, len)
      }
    }
    if (wasRead > 0) {
      data.position += wasRead
    }
    return wasRead
  }

  override fun setTcpNoDelay(value: Boolean): Boolean =
    data.use { ptr ->
      NSocket_setNoDelay(ptr, if (value) 1 else 0)
    } == 1

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
