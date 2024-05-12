package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.socket.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.InHeap
import kotlin.time.Duration

@OptIn(ExperimentalForeignApi::class)
actual class UdpNetSocket : UdpSocket, NetSocket {
  override val data = InHeap.create<NSocket>()

  init {
    data.use { ptr ->
      if (NSocket_create(ptr, NET_TYPE_INET4, SOCKET_TYPE_UDP) != 1) {
        throw IOException("Can't create socket")
      }
      if (NSocket_setBroadcastEnabled(ptr, 1) != 1) {
        throw IOException("Can't set broadcast enabled")
      }
    }
  }

  override val native: RawSocket = data.use { ptr -> ptr.pointed.native }

  actual fun bind(address: InetSocketAddress): BindStatus {
    val bindResult = data.use { socketPtr ->
      val r = address.native.use { addrPtr ->
        NSocket_bindInet(socketPtr, addrPtr, 0)
      }
      if (NSocket_setReuseaddr(socketPtr, 1) != 1) {
        throw IOException("Can't reuse socket")
      }
      r
    }
    return when (bindResult) {
      BIND_RESULT_OK -> BindStatus.OK
      BIND_RESULT_NOT_SUPPORTED -> throw IOException("Bind not supported")
      BIND_RESULT_ALREADY_BINDED -> BindStatus.ALREADY_BINDED
      BIND_RESULT_ADDRESS_ALREADY_IN_USE -> BindStatus.ADDRESS_ALREADY_IN_USE
      BIND_RESULT_UNKNOWN_ERROR -> BindStatus.UNKNOWN
      BIND_RESULT_PROTOCOL_NOT_SUPPORTED -> BindStatus.PROTOCOL_NOT_SUPPORTED
      else -> BindStatus.UNKNOWN
    }
  }

  actual fun send(data: ByteBuffer, address: InetSocketAddress): Int {
    if (!data.isReferenceAccessAvailable()) {
      return 0
    }
    val d = this.data.use { socketPtr ->
      address.native.use { addrPtr ->
        data.ref(0) { data, size ->
          NSocket_sendTo(socketPtr, addrPtr, data, size)
        }
      }
    }
    if (d > 0) {
      data.position += d
    }
    return d
  }

  actual fun receive(data: ByteBuffer, address: MutableInetSocketAddress?): Int {
    if (!data.isReferenceAccessAvailable()) {
      return 0
    }
    val received = this.data.use { socketPtr ->
      if (address == null) {
        data.ref(0) { data, len ->
          NSocket_receiveFrom(socketPtr, data, len, null)
        }
      } else {
        address.native.use { addrPtr ->
          data.ref(0) { data, len ->
            NSocket_receiveFrom(socketPtr, data, len, addrPtr)
          }
        }
      }
    }
    if (received > 0) {
      data.position += received
    }
    return received
  }

  actual var ttl: UByte
    get() = data.use { socketPtr ->
      memScoped {
        val value = alloc<UByteVar>()
        if (NSocket_getTTL(socketPtr, value.ptr) != 1) {
          throw IllegalStateException("Can't get TTL")
        }
        value.value
      }
    }
    set(value) {
      data.use { socketPtr ->
        if (NSocket_setTTL(socketPtr, value) != 1) {
          throw IllegalStateException("Can't set TTL")
        }
      }
    }

  override fun close() {
    data.use { NSocket_close(it) }
  }

  override fun setSoTimeout(duration: Duration) {
    data.use { socketPtr ->
      if (NSocket_setSoTimeout(socketPtr, duration.inWholeMilliseconds.toULong()) != 1) {
        throw IOException("Can't set soTimeout")
      }
    }
  }


  override var blocking: Boolean
    get() = data.use { NSocket_getBlockedMode(it) } == 1
    set(value) {
      data.use { NSocket_setBlockedMode(it, if (value) 1 else 0) }
    }

  override fun toString(): String = "UdpNetSocket($native)"
  override val server: Boolean
    get() = false
  override var keyHash: Int
    get() = TODO("Not yet implemented")
    set(value) {}
  override val id: String
    get() = TODO("Not yet implemented")
  override val tcpNoDelay: Boolean
    get() = data.use { NSocket_getNoDelay(it) } == 1

  override fun setTcpNoDelay(value: Boolean): Boolean {
    return data.use { NSocket_setNoDelay(it, if (value) 1 else 0) } == 1
  }

  override val port: Int?
    get() = data.use { socketPtr ->
      NSocket_getSocketPort(socketPtr)
    }.takeIf { it > 0 }
}
