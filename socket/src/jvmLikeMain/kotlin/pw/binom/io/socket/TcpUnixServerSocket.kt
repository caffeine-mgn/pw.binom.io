package pw.binom.io.socket

import java.nio.channels.AlreadyBoundException
import java.nio.channels.ServerSocketChannel
import java.nio.channels.spi.AbstractSelectableChannel

actual class TcpUnixServerSocket(override val native: ServerSocketChannel) : TcpServerSocket {
  actual constructor() : this(createTcpServerUnixSocket())

  actual fun accept(): TcpClientUnixSocket? {
    val socket = native.accept() ?: return null
    return TcpClientUnixSocket(socket)
  }

  actual fun bind(path: String): BindStatus =
    try {
      native.bindUnix(path)
      BindStatus.OK
    } catch (e: AlreadyBoundException) {
      BindStatus.ALREADY_BINDED
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
    get() = TODO("Not yet implemented")
  actual override val tcpNoDelay: Boolean
    get() = false

  actual override fun setTcpNoDelay(value: Boolean): Boolean = false
}
