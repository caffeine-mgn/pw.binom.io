package pw.binom.io.socket

import com.jakewharton.cite.__LINE__
import pw.binom.InternalLog
import pw.binom.io.ByteBuffer
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.nio.channels.AlreadyConnectedException
import java.nio.channels.SocketChannel

actual open class TcpClientNetSocket (override val native: SocketChannel) : TcpClientSocket, NetSocket {
  actual constructor():this(SocketChannel.open())
  private val logger = InternalLog.file("ClientSocket_$id")
  actual fun connect(address: InetSocketAddress): ConnectStatus {
    return try {
      logger.info(line = __LINE__) { "Start connecting to $address" }
      if (native.connect(address.native)) {
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

  override fun close() {
    logger.info(line = __LINE__) { "Closing" }
    runCatching { native.shutdownInput() }
    runCatching { native.shutdownOutput() }
    native.close()
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

  override fun receive(data: ByteBuffer): Int =
    try {
      val r = data.remaining
      val s = native.read(data.native)
      logger.info(line = __LINE__) { "Received $s/$r bytes" }
      s
    } catch (e: java.net.SocketException) {
      logger.info(line = __LINE__) { "Can't receive: $e" }
      -1
    }

  override var blocking: Boolean=false
    set(value) {
      field = value
      logger.info(line = __LINE__) { "set blocking to $value" }
      native.configureBlocking(value)
    }
  override val id: String
    get() = System.identityHashCode(native).toString()
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
  private var internalPort = 0
  override val port: Int?
    get() = internalPort.takeIf { it != 0 }
}
