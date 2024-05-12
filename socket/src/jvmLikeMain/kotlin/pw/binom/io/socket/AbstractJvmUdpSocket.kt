package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import java.net.InetSocketAddress as JvmInetSocketAddress
import java.nio.channels.DatagramChannel
import kotlin.time.Duration

abstract class AbstractJvmUdpSocket(override val native: DatagramChannel) : UdpSocket, UdpUnixSocket {
  override fun close() {
    native.close()
  }

  override val id: String
    get() = System.identityHashCode(native).toString()

  override var blocking: Boolean = false
    set(value) {
      field = value
      native.configureBlocking(value)
    }

  private var internalPort = 0

/*
  override val port: Int?
    get() = internalPort.takeIf { it != 0 }
*/

/*  override fun bind(address: InetSocketAddress): BindStatus {
    try {
      native.bind(address.native)
      internalPort = native.socket().localPort
      return BindStatus.OK
    } catch (e: java.net.BindException) {
      return BindStatus.ADDRESS_ALREADY_IN_USE
    }
  }*/

/*  override fun send(
    data: ByteBuffer,
    address: InetSocketAddress,
  ): Int {
    try {
      return native.send(data.native, address.native)
    } catch (e: java.net.SocketException) {
      throw IOException(e.message)
    }
  }*/

/*  override fun receive(
    data: ByteBuffer,
    address: MutableInetSocketAddress?,
  ): Int {
    val before = data.position
    if (before == data.remaining) {
      return 0
    }
    val remoteAddress = native.receive(data.native)
    if (remoteAddress != null && address != null) {
      remoteAddress as JvmInetSocketAddress
      address.native=remoteAddress
    }
    return data.position - before
  }*/

  override fun bind(path: String): BindStatus {
    native.socket().reuseAddress = true
    native.bindUnix(path)
    return BindStatus.OK
  }

  override fun send(
    data: ByteBuffer,
    address: String,
  ): Int = native.sendUnix(address, data)

  override fun receive(
    data: ByteBuffer,
    address: (String) -> Unit?,
  ): Int {
    val before = data.position
    native.receive(data.native)
    return data.position - before
  }

  override val tcpNoDelay: Boolean
    get() = false

  override fun setTcpNoDelay(value: Boolean): Boolean = false
  override fun setSoTimeout(duration: Duration) {
    native.socket().soTimeout = duration.inWholeMilliseconds.toInt()
  }
}
