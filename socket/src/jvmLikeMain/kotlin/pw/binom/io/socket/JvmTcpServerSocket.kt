package pw.binom.io.socket

import com.jakewharton.cite.__LINE__
import pw.binom.InternalLog
import java.net.Inet4Address
import java.nio.channels.AlreadyBoundException
import java.nio.channels.ServerSocketChannel

class JvmTcpServerSocket(override val native: ServerSocketChannel) : TcpNetServerSocket, TcpUnixServerSocket {
  override val id: String
    get() = System.identityHashCode(native).toString()

  private val logger = InternalLog.file("ServerSocket_$id")

  override fun close() {
    native.close()
  }

  override fun accept(address: MutableInetNetworkAddress?): TcpClientNetSocket? {
    logger.info(line = __LINE__) { "Accepting..." }
    val client = native.accept()
    if (client == null) {
      logger.info(line = __LINE__) { "No socket for accept" }
      return null
    }
    if (address != null) {
      val addr = client.socket().localAddress as Inet4Address
      address.update(
        host = addr.hostAddress,
        port = 0,
      )
    }
    val out = JvmTcpClientSocket(client)
    logger.info(line = __LINE__) { "Socket ${System.identityHashCode(client)} accepted" }
    return out
  }

  override fun bind(address: InetNetworkAddress): BindStatus {
    try {
      logger.info(line = __LINE__) { "Binding to $address" }
      native.socket().reuseAddress = true
      native.bind(address.toJvmAddress().native)
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

  override fun accept(address: ((String) -> Unit)?): TcpClientNetSocket? {
    val newClient = native.accept() ?: return null
    return JvmTcpClientSocket(newClient)
  }

  override fun bind(path: String): BindStatus {
    try {
      native.bindUnix(path)
    } catch (e: AlreadyBoundException) {
      return BindStatus.ALREADY_BINDED
    }
    return BindStatus.OK
  }

  override var blocking: Boolean = false
    set(value) {
      field = value
      native.configureBlocking(value)
    }
  override val port: Int?
    get() = native.socket().localPort.takeIf { it != -1 }
  override val tcpNoDelay: Boolean
    get() = false

  override fun setTcpNoDelay(value: Boolean): Boolean = false
}
