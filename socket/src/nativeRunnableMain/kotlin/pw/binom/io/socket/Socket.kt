package pw.binom.io.socket

import platform.posix.AF_INET6
import platform.posix.AF_UNIX
import platform.posix.SOCK_DGRAM
import platform.posix.SOCK_STREAM
import platform.posix.*
import pw.binom.io.Closeable
import platform.common.internal_create_socket

actual interface Socket : Closeable {
    actual var blocking: Boolean
    val native: RawSocket

    actual companion object {
        actual fun createTcpClientNetSocket(): TcpClientNetSocket {
            val native = internal_create_socket(AF_INET6, SOCK_STREAM, 0)
            allowIpv4(native)
            return createSocket(native) as TcpClientNetSocket
        }

        actual fun createTcpClientUnixSocket(): TcpClientUnixSocket {
            val native = internal_create_socket(AF_UNIX, SOCK_STREAM, 0)
            return createSocket(native) as TcpClientUnixSocket
        }

        actual fun createUdpNetSocket(): UdpNetSocket {
            val native = internal_create_socket(AF_INET6, SOCK_DGRAM, 0)
            allowIpv4(native)
            return createSocket(native) as UdpNetSocket
        }

        actual fun createUdpUnixSocket(): UdpUnixSocket {
            val native = internal_create_socket(AF_UNIX, SOCK_DGRAM, 0)
            return createSocket(native) as UdpUnixSocket
        }

        actual fun createTcpServerNetSocket(): TcpNetServerSocket {
            val native = internal_create_socket(AF_INET6, SOCK_STREAM, 0)
            allowIpv4(native)
            return createSocket(native) as TcpNetServerSocket
        }

        actual fun createTcpServerUnixSocket(): TcpUnixServerSocket {
            val native = internal_create_socket(AF_UNIX, SOCK_STREAM, 0)
            return createSocket(native) as TcpUnixServerSocket
        }
    }

    actual val tcpNoDelay: Boolean
    actual fun setTcpNoDelay(value: Boolean): Boolean
}
