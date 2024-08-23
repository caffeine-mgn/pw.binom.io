package pw.binom.io.socket

import pw.binom.InternalLog
import pw.binom.io.ByteBuffer
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.nio.channels.AlreadyConnectedException
import java.nio.channels.SocketChannel
import kotlin.math.absoluteValue

actual open class TcpClientNetSocket(override val native: SocketChannel) : TcpClientSocket, NetSocket {
  actual constructor() : this(SocketChannel.open())

  private val logger = InternalLog.file("TcpClientNetSocket")
  actual fun connect(address: InetSocketAddress): ConnectStatus {
    return try {
      logger.info { "Start connecting to $address" }
      if (native.connect(address.native)) {
        logger.info { "Connected success" }
        ConnectStatus.OK
      } else {
        logger.info { "Connection in process" }
        ConnectStatus.IN_PROGRESS
      }
    } catch (e: AlreadyConnectedException) {
      logger.info { "Already connected" }
      ConnectStatus.ALREADY_CONNECTED
    } catch (e: ConnectException) {
      logger.info { "Can't connect: connection_refused" }
      ConnectStatus.CONNECTION_REFUSED
    }
  }

  actual override fun close() {
    logger.info { "Closing" }
    runCatching { native.shutdownInput() }
    runCatching { native.shutdownOutput() }
    native.close()
  }

  actual override fun send(data: ByteBuffer): Int =
    try {
      val r = data.remaining
      val s = native.write(data.native) ?: throw IllegalStateException()
      logger.info { "Success send $s/$r bytes" }
      s
    } catch (e: IOException) {
      logger.info { "Can't send $e" }
//        throw RuntimeException("Can't write ${data.remaining}", e)
      -1
    }

  actual override fun receive(data: ByteBuffer): Int =
    try {
      val r = data.remaining
      val s = native.read(data.native)
      logger.info { "Received $s/$r bytes" }
      s
    } catch (e: java.net.SocketException) {
      logger.info { "Can't receive: $e" }
      -1
    }

  actual override var blocking: Boolean = false
    set(value) {
      field = value
      logger.info { "set blocking to $value" }
      native.configureBlocking(value)
    }
  actual override val id: String
    get() = System.identityHashCode(native).absoluteValue.toString()
  actual override val tcpNoDelay: Boolean
    get() =
      try {
        native.socket().tcpNoDelay
      } catch (e: SocketException) {
        false
      }

  actual override fun setTcpNoDelay(value: Boolean): Boolean =
    try {
      native.socket().tcpNoDelay = value
      logger.info { "set TcpNoDelay to $value" }
      true
    } catch (e: SocketException) {
      logger.info { "set TcpNoDelay to $value" }
      false
    }

  private var internalPort = 0
  actual override val port: Int?
    get() = internalPort.takeIf { it != 0 }
}
