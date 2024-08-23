package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.channels.spi.AbstractSelectableChannel

actual class TcpClientUnixSocket(override val native: SocketChannel) : TcpClientSocket {
  actual constructor() : this(createTcpClientUnixSocket())

  actual fun connect(path: String): ConnectStatus {
    native.connectUnix(path)
    return ConnectStatus.OK
  }

  actual override fun close() {
    native.close()
  }

  actual override fun send(data: ByteBuffer): Int =
    native.write(data.native)

  actual override fun receive(data: ByteBuffer): Int =
    native.read(data.native)

  actual override var blocking: Boolean = false
    set(value) {
      field = value
      native.configureBlocking(value)
    }
  actual override val id: String
    get() = TODO("Not yet implemented")
  actual override val tcpNoDelay: Boolean
    get() = native.socket().tcpNoDelay

  actual override fun setTcpNoDelay(value: Boolean): Boolean {
    native.socket().tcpNoDelay = value
    return true
  }
}
