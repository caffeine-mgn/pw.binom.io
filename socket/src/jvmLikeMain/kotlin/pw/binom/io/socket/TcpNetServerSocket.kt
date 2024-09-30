package pw.binom.io.socket

import pw.binom.InternalLog
import java.nio.channels.AlreadyBoundException
import java.nio.channels.ServerSocketChannel

actual class TcpNetServerSocket (override val native: ServerSocketChannel): TcpServerSocket, NetSocket {
  actual constructor():this(ServerSocketChannel.open())
  private val logger = InternalLog.file("TcpNetServerSocket")
  actual fun accept(address: MutableInetAddress?): TcpClientNetSocket? {
    logger.info(method = "accept") { "Accepting..." }
    val client = native.accept()
    if (client == null) {
      logger.info(method = "accept") { "No socket for accept" }
      return null
    }
    if (address != null) {
      address.native = client.socket().localAddress
    }
    logger.info(method = "accept") { "Socket ${System.identityHashCode(client)} accepted" }
    return TcpClientNetSocket(client)
  }

  actual fun bind(address: InetSocketAddress): BindStatus {
    try {
      logger.info(method = "bind") { "Binding to $address" }
      native.socket().reuseAddress = true
      native.bind(address.native)
    } catch (e: AlreadyBoundException) {
      logger.info(method = "bind") { "Bind ALREADY_BINDED" }
      return BindStatus.ALREADY_BINDED
    } catch (e: java.net.BindException) {
      logger.info(method = "bind") { "Bind ADDRESS_ALREADY_IN_USE" }
      return BindStatus.ADDRESS_ALREADY_IN_USE
    }
    logger.info(method = "bind") { "Bind OK" }
    return BindStatus.OK
  }

  actual override fun close() {
    native.close()
  }

  actual override var blocking: Boolean = false
    set(value) {
      field = value
      native.configureBlocking(value)
    }
  actual override val id: String
    get() = System.identityHashCode(native).toString()
  actual override val tcpNoDelay: Boolean
    get() = false

  actual override fun setTcpNoDelay(value: Boolean): Boolean = false

  actual override val port: Int?
    get() = native.socket().localPort.takeIf { it != -1 }
}
