package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import platform.socket.*
import pw.binom.io.ByteBuffer
import pw.binom.io.InHeap

@OptIn(ExperimentalForeignApi::class)
abstract class AbstractTcpSocket(init: Boolean) : TcpClientSocket {
  override val data = InHeap.create<NSocket>()
  protected abstract fun initSocket()

  override val id: String
    get() = TODO("Not yet implemented")

  override val server: Boolean
    get() = false

  override var keyHash: Int
    get() = TODO("Not yet implemented")
    set(value) {}

  override var blocking: Boolean
    get() = data.use { ptr ->
      NSocket_getBlockedMode(ptr) == 1
    }
    set(value) {
      data.use { ptr ->
        NSocket_setBlockedMode(ptr, if (value) 1 else 0)
      }
    }

  override val tcpNoDelay: Boolean
    get() = data.use { ptr ->
      NSocket_getNoDelay(ptr) == 1
    }

  override val native: RawSocket
    get() = data.use { ptr -> ptr.pointed.native }

  init {
    if (init) {
      initSocket()
    }
  }

  override fun close() {
    data.use { ptr ->
      NSocket_close(ptr)
    }
  }

  override fun setTcpNoDelay(value: Boolean): Boolean =
    data.use { ptr ->
      NSocket_setNoDelay(ptr, if (value) 1 else 0)
    } == 1

  override fun send(data: ByteBuffer): Int {
    if (!data.isReferenceAccessAvailable()) {
      return 0
    }
    val sent = this.data.use { socketPtr ->
      data.ref(0) { dataPtr, dataLen ->
        NSocket_send(socketPtr, dataPtr, dataLen)
      }
    }
    if (sent > 0) {
      data.position += sent
    }
    return sent
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
}
