package pw.binom.io.socket

import platform.posix.*
import pw.binom.io.Closeable

actual interface Socket : Closeable {
    actual var blocking: Boolean
    val native: RawSocket

    actual companion object {
        actual fun createTcpClientNetSocket(): TcpClientNetSocket {
            val native = socket(AF_INET6, SOCK_STREAM, 0)
            allowIpv4(native)
            return PosixSocket(native)
        }

        actual fun createTcpClientUnixSocket(): TcpClientUnixSocket {
            val native = socket(AF_UNIX, SOCK_STREAM, 0)
            return PosixSocket(native)
        }

        actual fun createUdpNetSocket(): UdpNetSocket {
            val native = socket(AF_INET6, SOCK_DGRAM, 0)
            allowIpv4(native)
            return PosixSocket(native)
        }

        actual fun createUdpUnixSocket(): UdpUnixSocket {
            val native = socket(AF_UNIX, SOCK_DGRAM, 0)
            return PosixSocket(native)
        }

        actual fun createTcpServerNetSocket(): TcpNetServerSocket {
            val native = socket(AF_INET6, SOCK_STREAM, 0)
            allowIpv4(native)
            return PosixSocket(native)
        }

        actual fun createTcpServerUnixSocket(): TcpUnixServerSocket {
            val native = socket(AF_UNIX, SOCK_STREAM, 0)
            return PosixSocket(native)
        }
    }
}
