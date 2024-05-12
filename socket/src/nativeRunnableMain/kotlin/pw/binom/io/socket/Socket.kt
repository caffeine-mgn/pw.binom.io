package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.common.internal_create_socket
import platform.common.internal_get_last_error
import platform.common.internal_setsockopt
import platform.posix.*
import platform.socket.NSocket
import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.io.InHeap

actual interface Socket : Closeable {
  @OptIn(ExperimentalForeignApi::class)
  val data:InHeap<NSocket>
  actual var blocking: Boolean
  val native: RawSocket
  val server: Boolean
  var keyHash: Int
  actual val id: String


  @OptIn(ExperimentalForeignApi::class)
  actual companion object {
/*
    actual fun createTcpClientNetSocket(): TcpClientNetSocket {
      TODO()
//      val native = internal_create_socket(AF_INET6, SOCK_STREAM, 0)
//      if (native <= 0) {
//        throw IOException("Can't create client tcp socket. error: ${internal_get_last_error()}")
//      }
//      allowIpv4(native)
//      return createSocket(socket = native, server = false) as TcpClientNetSocket
    }
*/

/*
    actual fun createTcpClientUnixSocket(): TcpClientUnixSocket {
      TODO()
//      val native = internal_create_socket(AF_UNIX, SOCK_STREAM, 0)
//      if (native <= 0) {
//        throw IOException("Can't create client tcp unix socket. error: ${internal_get_last_error()}")
//      }
//      return createSocket(socket = native, server = false) as TcpClientUnixSocket
    }
*/

/*    actual fun createUdpNetSocket(): UdpNetSocket {
      TODO()
//      val native = internal_create_socket(AF_INET6, SOCK_DGRAM, 0)
//      if (native <= 0) {
//        throw IOException("Can't create udp client socket. error: ${internal_get_last_error()}")
//      }
//      allowIpv4(native)
//      return createSocket(socket = native, server = false) as UdpNetSocket
    }*/

/*
    actual fun createUdpUnixSocket(): UdpUnixSocket {
      TODO()
//      val native = internal_create_socket(AF_UNIX, SOCK_DGRAM, 0)
//      if (native <= 0) {
//        throw IOException("Can't create udp client unix socket. error: ${internal_get_last_error()}")
//      }
//      return createSocket(socket = native, server = false) as UdpUnixSocket
    }
*/

/*
    actual fun createTcpServerNetSocket(): TcpNetServerSocket {
      TODO()
//      val native = internal_create_socket(AF_INET6, SOCK_STREAM, 0)
//      if (native <= 0) {
//        throw IOException("Can't create server tcp socket. error: ${internal_get_last_error()}")
//      }
//      allowIpv4(native)
//      return createSocket(socket = native, server = true) as TcpNetServerSocket
    }
*/

/*
    actual fun createTcpServerUnixSocket(): TcpUnixServerSocket {
      TODO()
//      val native = internal_create_socket(AF_UNIX, SOCK_STREAM, 0)
//      if (native <= 0) {
//        throw IOException("Can't create server tcp unix socket. error: ${internal_get_last_error()}")
//      }
//      return createSocket(socket = native, server = true) as TcpUnixServerSocket
    }
*/

/*    actual fun createMulticastSocket(port: Int, networkInterface: NetworkInterface): MulticastSocket {
      TODO()
//      val native = internal_create_socket(AF_INET6, SOCK_DGRAM, 0)
//      if (native <= 0) {
//        throw IOException("Can't create udp client socket. error: ${internal_get_last_error()}")
//      }
//      allowIpv4(native)
//      memScoped {
//        val opt = alloc<IntVar>()
//        opt.value = 1
//        if (internal_setsockopt(native, SOL_SOCKET, SO_REUSEADDR, opt.ptr, sizeOf<IntVar>().convert()) == -1) {
//          throw IOException("Can't set SO_REUSEADDR for socket")
//        }
//      }
//
//      val socket = createSocket(socket = native, server = false) as MulticastSocket
//      return socket
    }*/
  }

  actual val tcpNoDelay: Boolean

  actual fun setTcpNoDelay(value: Boolean): Boolean
}
