package pw.binom.io.socket

import pw.binom.io.Closeable

expect interface Socket : Closeable {
    var blocking: Boolean

    companion object {
        fun createTcpClientNetSocket(): TcpClientNetSocket
        fun createTcpClientUnixSocket(): TcpClientUnixSocket
        fun createTcpServerNetSocket(): TcpNetServerSocket
        fun createTcpServerUnixSocket(): TcpUnixServerSocket
        fun createUdpNetSocket(): UdpNetSocket
        fun createUdpUnixSocket(): UdpUnixSocket
    }
}
