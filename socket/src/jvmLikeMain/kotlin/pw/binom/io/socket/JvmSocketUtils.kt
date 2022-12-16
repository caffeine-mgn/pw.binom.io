package pw.binom.io.socket

import java.nio.channels.DatagramChannel
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

expect fun createTcpClientUnixSocket(): SocketChannel
expect fun createTcpServerUnixSocket(): ServerSocketChannel
expect fun createUdpUnixSocket(): DatagramChannel
