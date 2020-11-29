package pw.binom.network

import pw.binom.io.Closeable
import java.net.ConnectException
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.Selector as JSelector

actual class Selector actual constructor() : Closeable {
    actual companion object {
        actual val EVENT_EPOLLIN: Int = 0b1
        actual val EVENT_EPOLLOUT: Int = 0b10
        actual val EVENT_EPOLLRDHUP: Int = 0b100
        actual val EVENT_CONNECTED: Int = 0b1000
        actual val EVENT_ERROR: Int = 0b10000
    }

    private val native = JSelector.open()

    private fun commonToJava(channel: SocketChannel, mode: Int): Int {
        var opts = 0
        if (Selector.EVENT_EPOLLIN and mode != 0) {
            if (channel is ServerSocketChannel) {
                opts = opts or SelectionKey.OP_ACCEPT
            } else {
                opts = opts or SelectionKey.OP_READ
            }
        }

        if (Selector.EVENT_EPOLLOUT and mode != 0) {
            require(channel is SocketChannel)
            opts = opts or SelectionKey.OP_WRITE
        }
        return opts
    }

    actual fun attach(socket: TcpClientSocketChannel, mode: Int, attachment: Any?) {
        socket.native.register(native, SelectionKey.OP_CONNECT or commonToJava(socket.native, mode))
    }

    actual fun attach(socket: TcpServerSocketChannel, mode: Int, attachment: Any?) {

    }

    actual fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?) {

    }

    actual fun mode(socket: TcpClientSocketChannel, mode: Int, attachment: Any?) {

    }

    actual fun mode(socket: TcpServerSocketChannel, mode: Int, attachment: Any?) {

    }

    actual fun mode(socket: UdpSocketChannel, mode: Int, attachment: Any?) {

    }

    actual fun wait(timeout: Long, func: (Any?, mode: Int) -> Unit): Boolean {
        when {
            timeout > 0L -> native.select(timeout)
            timeout == 0L -> native.selectNow()
            timeout < 0L -> native.select()
        }

        val keys = native.selectedKeys()
        if (keys.isEmpty()) {
            return false
        }
        var count = 0
        keys.forEach {
            if (it.isConnectable) {
                val cc = it.channel() as SocketChannel
                if (cc.isConnectionPending) {
                    try {
                        println("cc.isConnected=${cc.isConnected}")
                        val r = cc.finishConnect()
                        println("-->finishConnect=$r   cc.isConnected=${cc.isConnected}")
                        if (r) {
                            it.interestOps((it.interestOps().inv() or SelectionKey.OP_CONNECT).inv())
                            func(it.attachment(), Selector.EVENT_CONNECTED)
                            count++
                        } else {
                            return@forEach
                        }
                    } catch (e: ConnectException) {
                        func(it.attachment(), Selector.EVENT_ERROR)
                        count++
                        return@forEach
                    }
                }
            }
            println("it.isAcceptable->${it.isAcceptable}")
            println("it.isConnectable->${it.isConnectable}")
            println("it.isReadable->${it.isReadable}")
            println("it.isWritable->${it.isWritable}")
            println("it.isValid->${it.isValid}")
            println("isConnected->${(it.channel() as SocketChannel).isConnected}")
            println("isConnectionPending->${(it.channel() as SocketChannel).isConnectionPending}")
        }
        return count > 0
    }

    override fun close() {
        native.close()
    }
}