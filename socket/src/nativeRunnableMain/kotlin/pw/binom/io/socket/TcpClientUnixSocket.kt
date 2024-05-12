package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import platform.posix.errno
import platform.socket.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.InHeap

@OptIn(ExperimentalForeignApi::class)
actual class TcpClientUnixSocket(init: Boolean) : TcpClientSocket {
  actual constructor() : this(true)

  override val data = InHeap.create<NSocket>()

  init {
    if (init) {
      data.use { ptr ->
        if (NSocket_create(ptr, NET_TYPE_UNIX, SOCKET_TYPE_TCP) != 1) {
          throw IOException("Can't create socket")
        }
      }
    }
  }

  override val native: RawSocket = data.use { ptr -> ptr.pointed.native }

  actual fun connect(path: String): ConnectStatus {
    val result = data.use { socketPtr ->
      NSocket_connectUnix(socketPtr, path)
    }
    return when (result) {
      ConnectStatus_OK -> ConnectStatus.OK
      ConnectStatus_CONNECTION_REFUSED -> ConnectStatus.CONNECTION_REFUSED
      ConnectStatus_ALREADY_CONNECTED -> ConnectStatus.ALREADY_CONNECTED
      ConnectStatus_IN_PROGRESS -> ConnectStatus.IN_PROGRESS
      else -> throw IOException("Can't connect to \"$path\" $errno")
    }
  }

  override fun close() {
    data.use { socketPtr ->
      NSocket_close(socketPtr)
    }
  }

  override fun send(data: ByteBuffer): Int {
    if (!data.isReferenceAccessAvailable()) {
      return 0
    }
    return this.data.use { socketPtr ->
      data.ref(0) { data, len ->
        NSocket_send(socketPtr, data, len)
      }
    }
  }

  override fun receive(data: ByteBuffer): Int {
    if (!data.isReferenceAccessAvailable()) {
      return 0
    }
    val wasRead = this.data.use { socketPtr ->
      data.ref(0) { data, len ->
        NSocket_receiveOnly(socketPtr, data, len)
      }
    }
    if (wasRead > 0) {
      data.position += wasRead
    }
    return wasRead
  }

  override var blocking: Boolean
    get() = data.use { socketPtr ->
      NSocket_getBlockedMode(socketPtr) == 1
    }
    set(value) {
      data.use { socketPtr ->
        NSocket_setBlockedMode(socketPtr, if (value) 1 else 0)
      }
    }
  override val server: Boolean
    get() = false
  override var keyHash: Int
    get() = TODO("Not yet implemented")
    set(value) {}
  override val id: String
    get() = TODO("Not yet implemented")
  override val tcpNoDelay: Boolean
    get() = data.use { socketPtr ->
      NSocket_getNoDelay(socketPtr) == 1
    }

  override fun setTcpNoDelay(value: Boolean): Boolean =
    data.use { socketPtr ->
      NSocket_setNoDelay(socketPtr, if (value) 1 else 0)
    } == 1
}
