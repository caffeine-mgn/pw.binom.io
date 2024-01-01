package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import java.io.IOException
import java.net.ConnectException
import java.net.SocketException
import java.nio.channels.AlreadyConnectedException
import java.nio.channels.SocketChannel

class JvmTcpClientSocket(
  override val native: SocketChannel,
) : Socket, TcpClientSocket, TcpClientUnixSocket, TcpClientNetSocket {
  override fun close() {
    println("JvmTcpClientSocket::close Closing socket $native")
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
    val netAddress = if (address is JvmMutableInetNetworkAddress) {
      address
    } else {
      JvmMutableInetNetworkAddress(address)
    }
    return try {
      if (native.connect(netAddress.native)) {
        ConnectStatus.OK
      } else {
        ConnectStatus.IN_PROGRESS
      }
    } catch (e: AlreadyConnectedException) {
      ConnectStatus.ALREADY_CONNECTED
    } catch (e: ConnectException) {
      ConnectStatus.CONNECTION_REFUSED
    }
  }

  override fun send(data: ByteBuffer): Int = try {
    native.write(data.native) ?: throw IllegalStateException()
  } catch (e: IOException) {
//        throw RuntimeException("Can't write ${data.remaining}", e)
    -1
  }

  override fun receive(data: ByteBuffer): Int {
    try {
      return native.read(data.native)
    } catch (e: java.net.SocketException) {
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
      native.configureBlocking(value)
    }
  override val tcpNoDelay: Boolean
    get() = try {
      native.socket().tcpNoDelay
    } catch (e: SocketException) {
      false
    }

  override fun setTcpNoDelay(value: Boolean): Boolean =
    try {
      native.socket().tcpNoDelay = value
      true
    } catch (e: SocketException) {
      false
    }
}
