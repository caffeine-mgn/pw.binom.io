package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.errno
import platform.socket.*
import pw.binom.io.IOException
import pw.binom.io.InHeap

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TcpNetServerSocket actual constructor() : TcpServerSocket, NetSocket {

  override val data = InHeap.create<NSocket>()

  init {
    data.use { ptr ->
      if (NSocket_create(ptr, NET_TYPE_INET6, SOCKET_TYPE_TCP) != 1) {
        throw IOException("Can't create socket")
      }
    }
  }

  actual fun accept(address: MutableInetAddress?): TcpClientNetSocket? {
    val newNativeSocket = if (address != null) {
      val tmpSocketAdderPtr = nativeHeap.alloc<NInetSocketNetworkAddress>()
      try {
        val acceptSocket = data.use { socketPtr -> NSocket_acceptInetSocketAddress(socketPtr, tmpSocketAdderPtr.ptr) }
        if (acceptSocket > 0) {
          address.native.use { aaa ->
            if (NInetSocketNetworkAddress_getHost(tmpSocketAdderPtr.ptr, aaa) != 1) {
              throw IOException("Can't get host from address $errno")
            }
          }
        }
        acceptSocket
      } finally {
        nativeHeap.free(tmpSocketAdderPtr)
      }
    } else {
      data.use { NSocket_acceptInetSocketAddress(it, null) }
    }
    if (newNativeSocket <= 0) {
      return null
    } else {
      val newSocket = TcpClientNetSocket(false)
      val protocolFamily = data.use { it.pointed.protocolFamily }
      newSocket.data.use { ptr ->
        ptr.pointed.native = newNativeSocket
        ptr.pointed.type = SOCKET_TYPE_TCP
        ptr.pointed.protocolFamily = protocolFamily
      }
      return newSocket
    }
  }

  actual fun bind(address: InetSocketAddress): BindStatus {
    val result = data.use { ptr ->
      address.native.use { addrPtr ->
        NSocket_bindInet(ptr, addrPtr, 1)
      }
    }
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
    data.use { ptr ->
      NSocket_close(ptr)
    }
  }


  actual override var blocking: Boolean
    get() = data.use { ptr ->
      NSocket_getBlockedMode(ptr) > 0
    }
    set(value) {
      data.use { ptr ->
        NSocket_setBlockedMode(ptr, if (value) 1 else 0)
      }
    }
  override val native: RawSocket = data.use { ptr -> ptr.pointed.native }
  override val server: Boolean
    get() = true
  override var keyHash: Int
    get() = TODO("Not yet implemented")
    set(value) {}
  actual override val id: String
    get() = TODO("Not yet implemented")
  actual override val tcpNoDelay: Boolean
    get() = data.use { ptr ->
      NSocket_getNoDelay(ptr) > 0
    }

  actual override fun setTcpNoDelay(value: Boolean): Boolean =
    data.use { ptr ->
      NSocket_setNoDelay(ptr, if (value) 1 else 0)
    } > 0

  actual  override val port: Int?
    get() = data.use { ptr ->
      NSocket_getSocketPort(ptr).takeIf { it > 0 }
    }
}

