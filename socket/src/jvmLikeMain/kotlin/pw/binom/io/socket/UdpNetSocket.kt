package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import java.net.StandardSocketOptions
import java.nio.channels.DatagramChannel
import kotlin.time.Duration
import java.net.InetSocketAddress as JvmInetSocketAddress

actual class UdpNetSocket(override val native: DatagramChannel) : UdpSocket, NetSocket {
  actual constructor() : this(DatagramChannel.open())

  actual fun bind(address: InetSocketAddress): BindStatus {
    try {
      native.bind(address.native)
      return BindStatus.OK
    } catch (e: java.net.BindException) {
      return BindStatus.ADDRESS_ALREADY_IN_USE
    }
  }

  actual fun send(data: ByteBuffer, address: InetSocketAddress): Int {
    try {
      return native.send(data.native, address.native)
    } catch (e: java.net.SocketException) {
      throw IOException(e.message)
    }
  }

  actual fun receive(data: ByteBuffer, address: MutableInetSocketAddress?): Int {
    val before = data.position
    if (before == data.remaining) {
      return 0
    }
    val remoteAddress = native.receive(data.native)
    if (remoteAddress != null && address != null) {
      remoteAddress as JvmInetSocketAddress
      address.native = remoteAddress
    }
    return data.position - before
  }

  override fun close() {
    native.close()
  }

  override fun setSoTimeout(duration: Duration) {
    native.socket().soTimeout = duration.inWholeMilliseconds.toInt()
  }

  override var blocking: Boolean = false
    set(value) {
      field = value
      native.configureBlocking(value)
    }
  override val id: String
    get() = TODO("Not yet implemented")
  override val tcpNoDelay: Boolean
    get() = false

  override fun setTcpNoDelay(value: Boolean): Boolean = false

  override val port: Int?
    get() = native.socket().localPort.takeIf { it > 0 }
  actual var ttl: UByte
    get() = native.socket().getOption(StandardSocketOptions.IP_MULTICAST_TTL).toUByte()
    set(value) {
      native.socket().setOption(StandardSocketOptions.IP_MULTICAST_TTL, value.toInt())
    }
}
