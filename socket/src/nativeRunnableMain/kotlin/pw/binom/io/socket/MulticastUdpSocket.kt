package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import platform.socket.*
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.InHeap
import kotlin.time.Duration

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MulticastUdpSocket actual constructor(
  networkInterface: NetworkInterface,
  port: Int,
) : UdpSocket, NetSocket {
  override val data = InHeap.create<NSocket>()

  init {
    val type = when (val pf = networkInterface.ip.protocolFamily) {
      ProtocolFamily.AF_INET -> NET_TYPE_INET4
      ProtocolFamily.AF_INET6 -> NET_TYPE_INET6
      else -> throw IllegalArgumentException("ProtocolFamily $pf not unsupported")
    }
    data.use { ptr ->
      if (NSocket_create(ptr, type, SOCKET_TYPE_UDP) != 1) {
        throw IOException("Can't create socket")
      }
      networkInterface.ip.native.use { addressPtr ->
        if (NSocket_setMulticastInterface(ptr, addressPtr, networkInterface.name) != 1) {
          throw IOException("Can't set multicast interface to $networkInterface")
        }
      }
    }
  }

  override val native = data.use { ptr -> ptr.pointed.native }

  actual fun send(data: ByteBuffer, address: InetSocketAddress): Int {
    if (!data.isReferenceAccessAvailable()) {
      return 0;
    }
    val sent = this.data.use { socketPtr ->
      address.native.use { addressPtr ->
        data.ref(0) { dataPtr, len ->
          NSocket_sendTo(socketPtr, addressPtr, dataPtr, len)
        }
      }
    }
    if (sent > 0) {
      data.position += sent
    }
    return sent
  }

  actual fun receive(data: ByteBuffer, address: MutableInetSocketAddress?): Int {
    if (!data.isReferenceAccessAvailable()) {
      return 0
    }
    val received = this.data.use { socketPtr ->
      if (address != null) {
        address.native.use { addressPtr ->
          data.ref(0) { dataPtr, len ->
            NSocket_receiveFrom(socketPtr, dataPtr, len, addressPtr)
          }
        }
      } else {
        data.ref(0) { dataPtr, len ->
          NSocket_receiveFrom(socketPtr, dataPtr, len, null)
        }
      }
    }
    if (received > 0) {
      data.position += received
    }
    return received
  }

  actual fun setTtl(value: UByte) {
    data.use { socketPtr ->
      if (NSocket_setTTL(socketPtr, value) != 1) {
        throw IOException("Can't set TTL to $value")
      }
    }
  }

  override fun toString(): String = "MulticastUdpSocket($native)"

  actual fun joinGroup(address: InetAddress) {
    println("MulticastUdpSocket::joinGroup")
    TODO()
  }

  actual fun joinGroup(address: InetSocketAddress, netIf: NetworkInterface) {
    address.native.use { addressPtr ->
      data.use { socketPtr ->
        netIf.ip.native.use { ifAddrPtr ->
          if (NSocket_joinGroup(socketPtr, addressPtr, ifAddrPtr, netIf.name) != 1) {
            throw IOException("Can't join group")
          }
        }
      }
    }
  }

  actual fun leaveGroup(address: InetAddress) {
    TODO()
  }

  actual fun leaveGroup(address: InetSocketAddress, netIf: NetworkInterface) {
    TODO()
  }

  actual override fun close() {
    data.use { socketPtr ->
      NSocket_close(socketPtr)
    }
  }

  actual override fun setSoTimeout(duration: Duration) {
    data.use { socketPtr ->
      if (NSocket_setSoTimeout(socketPtr, duration.inWholeMilliseconds.toULong()) != 1) {
        throw IOException("Can't set SoTimeout $duration")
      }
    }
  }

  actual override var blocking: Boolean
    get() = data.use { NSocket_getBlockedMode(it) == 1 }
    set(value) {
      data.use { NSocket_setBlockedMode(it, if (value) 1 else 0) }
    }
  override val server: Boolean
    get() = false
  override var keyHash: Int
    get() = TODO("Not yet implemented")
    set(value) {}
  actual override val id: String
    get() = TODO("Not yet implemented")
  actual override val tcpNoDelay: Boolean
    get() = TODO("Not yet implemented")

  actual override fun setTcpNoDelay(value: Boolean): Boolean =
    data.use { socketPtr ->
      NSocket_setNoDelay(socketPtr, if (value) 1 else 0) == 1
    }

  actual override val port: Int?
    get() = data.use { socketPtr ->
      NSocket_getSocketPort(socketPtr)
    }.takeIf { it > 0 }
}
