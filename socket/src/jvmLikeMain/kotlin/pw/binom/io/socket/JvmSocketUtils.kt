package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

expect fun createTcpClientUnixSocket(): SocketChannel
expect fun createTcpServerUnixSocket(): ServerSocketChannel
expect fun createUdpUnixSocket(): DatagramChannel
expect fun SocketChannel.connectUnix(path: String): Boolean
expect fun ServerSocketChannel.bindUnix(path: String)
expect fun DatagramChannel.sendUnix(path: String, data: ByteBuffer): Int
expect fun DatagramChannel.bindUnix(path: String)
