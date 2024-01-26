package pw.binom.io.socket

import com.jakewharton.cite.__LINE__
import pw.binom.InternalLog
import pw.binom.io.ByteBuffer
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.nio.channels.AlreadyConnectedException
import java.nio.channels.SocketChannel

class JvmTcpClientSocket(
  override val native: SocketChannel,
) : Socket, TcpClientSocket, TcpClientUnixSocket, TcpClientNetSocket {
  override val id: String
    get() = System.identityHashCode(native).toString()

  private val logger = InternalLog.file("ClientSocket_$id")

  override fun close() {
    logger.info(line = __LINE__) { "Closing" }
    runCatching { native.shutdownInput() }
    runCatching { native.shutdownOutput() }
    native.close()
  }

  private var internalPort = 0

  override val port: Int?
    get() = internalPort.takeIf { it != 0 }

  // clientNative?.socket()?.localPort ?: serverNative?.socket()?.localPort

//    override fun accept(address: MutableNetworkAddress?): TcpClientNetSocket? {
//        val newNativeClient = serverNative?.accept() ?: return null
//        if (address != null) {
//            val clientAddress = newNativeClient.remoteAddress as InetSocketAddress
//            address.update(
//                host = clientAddress.address.hostAddress,
//                port = clientAddress.port,
//            )
//        }
//        return JvmTcpClientSocket(newNativeClient)
//    }

//    override fun bind(address: NetworkAddress): BindStatus {
//        if (internalPort != 0) {
//            return BindStatus.ALREADY_BINDED
//        }
//        val serverNative = serverNative ?: throw IllegalStateException()
//        serverNative.bind(address.toJvmAddress().native)
//        return BindStatus.OK
//    }

  override fun connect(address: InetNetworkAddress): ConnectStatus {
    val netAddress =
      if (address is JvmMutableInetNetworkAddress) {
        address
      } else {
        JvmMutableInetNetworkAddress(address)
      }
    return try {
      logger.info(line = __LINE__) { "Start connecting to $address" }
      if (native.connect(netAddress.native)) {
        logger.info(line = __LINE__) { "Connected success" }
        ConnectStatus.OK
      } else {
        logger.info(line = __LINE__) { "Connection in process" }
        ConnectStatus.IN_PROGRESS
      }
    } catch (e: AlreadyConnectedException) {
      logger.info(line = __LINE__) { "Already connected" }
      ConnectStatus.ALREADY_CONNECTED
    } catch (e: ConnectException) {
      logger.info(line = __LINE__) { "Can't connect: connection_refused" }
      ConnectStatus.CONNECTION_REFUSED
    }
  }

  override fun send(data: ByteBuffer): Int =
    try {
      val r = data.remaining
      val s = native.write(data.native) ?: throw IllegalStateException()
      logger.info(line = __LINE__) { "Success send $s/$r bytes" }
      s
    } catch (e: IOException) {
      logger.info(line = __LINE__) { "Can't send $e" }
//        throw RuntimeException("Can't write ${data.remaining}", e)
      -1
    }

  override fun receive(data: ByteBuffer): Int {
    try {
      val r = data.remaining
      val s = native.read(data.native)
      logger.info(line = __LINE__) { "Received $s/$r bytes" }
      return s
    } catch (e: java.net.SocketException) {
      logger.info(line = __LINE__) { "Can't receive: $e" }
      return -1
    }
  }

  override fun connect(path: String): ConnectStatus {
    native.connectUnix(path)
    return ConnectStatus.OK
  }

  override var blocking: Boolean = false
    set(value) {
      field = value
      logger.info(line = __LINE__) { "set blocking to $value" }
      native.configureBlocking(value)
    }
  override val tcpNoDelay: Boolean
    get() =
      try {
        native.socket().tcpNoDelay
      } catch (e: SocketException) {
        false
      }

  override fun setTcpNoDelay(value: Boolean): Boolean =
    try {
      native.socket().tcpNoDelay = value
      logger.info(line = __LINE__) { "set TcpNoDelay to $value" }
      true
    } catch (e: SocketException) {
      logger.info(line = __LINE__) { "set TcpNoDelay to $value" }
      false
    }
}
