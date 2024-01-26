package pw.binom.io.socket

import kotlinx.cinterop.ExperimentalForeignApi
import platform.common.internal_create_socket
import platform.common.internal_get_last_error
import platform.posix.AF_INET6
import platform.posix.AF_UNIX
import platform.posix.SOCK_DGRAM
import platform.posix.SOCK_STREAM
import pw.binom.io.Closeable
import pw.binom.io.IOException

actual interface Socket : Closeable {
  actual var blocking: Boolean
  val native: RawSocket
  val server: Boolean
  var keyHash: Int
  actual val id: String

  @OptIn(ExperimentalForeignApi::class)
  actual companion object {
    actual fun createTcpClientNetSocket(): TcpClientNetSocket {
      val native = internal_create_socket(AF_INET6, SOCK_STREAM, 0)
      if (native <= 0) {
        throw IOException("Can't create client tcp socket. error: ${internal_get_last_error()}")
      }
      allowIpv4(native)
      return createSocket(socket = native, server = false) as TcpClientNetSocket
    }

    actual fun createTcpClientUnixSocket(): TcpClientUnixSocket {
      val native = internal_create_socket(AF_UNIX, SOCK_STREAM, 0)
      if (native <= 0) {
        throw IOException("Can't create client tcp unix socket. error: ${internal_get_last_error()}")
      }
      return createSocket(socket = native, server = false) as TcpClientUnixSocket
    }

    actual fun createUdpNetSocket(): UdpNetSocket {
      val native = internal_create_socket(AF_INET6, SOCK_DGRAM, 0)
      if (native <= 0) {
        throw IOException("Can't create udp client socket. error: ${internal_get_last_error()}")
      }
      allowIpv4(native)
      return createSocket(socket = native, server = false) as UdpNetSocket
    }

    actual fun createUdpUnixSocket(): UdpUnixSocket {
      val native = internal_create_socket(AF_UNIX, SOCK_DGRAM, 0)
      if (native <= 0) {
        throw IOException("Can't create udp client unix socket. error: ${internal_get_last_error()}")
      }
      return createSocket(socket = native, server = false) as UdpUnixSocket
    }

    actual fun createTcpServerNetSocket(): TcpNetServerSocket {
      val native = internal_create_socket(AF_INET6, SOCK_STREAM, 0)
      if (native <= 0) {
        throw IOException("Can't create server tcp socket. error: ${internal_get_last_error()}")
      }
      allowIpv4(native)
      return createSocket(socket = native, server = true) as TcpNetServerSocket
    }

    actual fun createTcpServerUnixSocket(): TcpUnixServerSocket {
      val native = internal_create_socket(AF_UNIX, SOCK_STREAM, 0)
      if (native <= 0) {
        throw IOException("Can't create server tcp unix socket. error: ${internal_get_last_error()}")
      }
      return createSocket(socket = native, server = true) as TcpUnixServerSocket
    }
  }

  actual val tcpNoDelay: Boolean

  actual fun setTcpNoDelay(value: Boolean): Boolean
}
