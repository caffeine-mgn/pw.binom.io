package pw.binom.io.socket

import com.jakewharton.cite.__FILE__
import com.jakewharton.cite.__LINE__
import pw.binom.InternalLog
import java.nio.channels.AlreadyBoundException
import java.nio.channels.ServerSocketChannel
import java.nio.channels.spi.AbstractSelectableChannel

actual class TcpNetServerSocket (override val native: ServerSocketChannel): TcpServerSocket, NetSocket {
  actual constructor():this(ServerSocketChannel.open())
  private val logger = InternalLog.file(__FILE__)
  actual fun accept(address: MutableInetAddress?): TcpClientNetSocket? {
    logger.info(line = __LINE__) { "Accepting..." }
    val client = native.accept()
    if (client == null) {
      logger.info(line = __LINE__) { "No socket for accept" }
      return null
    }
    if (address != null) {
      address.native = client.socket().localAddress
    }
    logger.info(line = __LINE__) { "Socket ${System.identityHashCode(client)} accepted" }
    return TcpClientNetSocket(client)
  }

  actual fun bind(address: InetSocketAddress): BindStatus {
    try {
      logger.info(line = __LINE__) { "Binding to $address" }
      native.socket().reuseAddress = true
      native.bind(address.native)
    } catch (e: AlreadyBoundException) {
      logger.info(line = __LINE__) { "Bind ALREADY_BINDED" }
      return BindStatus.ALREADY_BINDED
    } catch (e: java.net.BindException) {
      logger.info(line = __LINE__) { "Bind ADDRESS_ALREADY_IN_USE" }
      return BindStatus.ADDRESS_ALREADY_IN_USE
    }
    logger.info(line = __LINE__) { "Bind OK" }
    return BindStatus.OK
  }

  override fun close() {
    native.close()
  }

  override var blocking: Boolean = false
    set(value) {
      field = value
      native.configureBlocking(value)
    }
  override val id: String
    get() = System.identityHashCode(native).toString()
  override val tcpNoDelay: Boolean
    get() = false

  override fun setTcpNoDelay(value: Boolean): Boolean = false

  override val port: Int?
    get() = native.socket().localPort.takeIf { it != -1 }
}
