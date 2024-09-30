package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import platform.posix.va_list
import platform.socket.*
import pw.binom.io.IOException
import pw.binom.io.InHeap

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TcpUnixServerSocket(init: Boolean) : TcpServerSocket {
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

  override val native = data.use { ptr -> ptr.pointed.native }

  actual fun accept(): TcpClientUnixSocket? {
    val newSocket = data.use { NSocket_acceptInetSocketAddress(it, null) }
    if (newSocket <= 0) {
      return null
    }
    val socket = TcpClientUnixSocket(false)
    socket.data.use {
      it.pointed.native = newSocket
      it.pointed.protocolFamily = NET_TYPE_UNIX
      it.pointed.type = SOCKET_TYPE_TCP
    }
    return socket
  }

  actual fun bind(path: String): BindStatus {
    val result = data.use { NSocket_bindUnix(it, path, 1) }
    return when (result) {
      BIND_RESULT_OK -> BindStatus.OK
      BIND_RESULT_NOT_SUPPORTED -> throw IOException("Bind not supported")
      BIND_RESULT_ALREADY_BINDED -> BindStatus.ALREADY_BINDED
      BIND_RESULT_ADDRESS_ALREADY_IN_USE -> BindStatus.ADDRESS_ALREADY_IN_USE
      BIND_RESULT_UNKNOWN_ERROR -> BindStatus.UNKNOWN
      BIND_RESULT_PROTOCOL_NOT_SUPPORTED -> BindStatus.PROTOCOL_NOT_SUPPORTED
      else -> BindStatus.UNKNOWN
    }
  }

  actual override fun close() {
    data.use { socketPtr ->
      NSocket_close(socketPtr)
    }
  }

  actual override var blocking: Boolean
    get() = data.use { NSocket_getBlockedMode(it) == 1 }
    set(value) {
      data.use { NSocket_setBlockedMode(it, if (value) 1 else 0) }
    }
  override val server: Boolean
    get() = true
  override var keyHash: Int
    get() = TODO("Not yet implemented")
    set(value) {}
  actual override val id: String
    get() = TODO("Not yet implemented")
  actual override val tcpNoDelay: Boolean
    get() = data.use { NSocket_getNoDelay(it) == 1 }

  actual override fun setTcpNoDelay(value: Boolean): Boolean =
    data.use { NSocket_setNoDelay(it, if (value) 1 else 0) } != 1
}

