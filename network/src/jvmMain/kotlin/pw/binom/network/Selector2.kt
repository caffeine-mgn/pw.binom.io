package pw.binom.network

import pw.binom.io.Closeable
import java.net.ConnectException
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.Selector as JSelector

actual class Selector2 actual constructor() : Closeable {
    actual companion object {
        actual val EVENT_EPOLLIN: Int = 0b1
        actual val EVENT_EPOLLOUT: Int = 0b10
        actual val EVENT_CONNECTED: Int = 0b1000
        actual val EVENT_ERROR: Int = 0b10000
    }

    private val native = JSelector.open()



    actual fun attach(socket: TcpClientSocketChannel, mode: Int, attachment: Any?) {
//        socket.native.register(native, SelectionKey.OP_CONNECT or commonToJava(socket.native, mode), attachment)
    }

    actual fun attach(socket: TcpServerSocketChannel, mode: Int, attachment: Any?) {

    }

    actual fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?) {

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
                            func(it.attachment(), Selector2.EVENT_CONNECTED)
                            count++
                        } else {
                            return@forEach
                        }
                    } catch (e: ConnectException) {
                        func(it.attachment(), Selector2.EVENT_ERROR)
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
            var opt = 0
            if (SelectionKey.OP_ACCEPT and it.interestOps() != 0 || SelectionKey.OP_READ and it.interestOps() != 0) {
                opt = opt or Selector2.EVENT_EPOLLIN
            }
            if (SelectionKey.OP_WRITE and it.interestOps() != 0) {
                opt = opt or Selector2.EVENT_EPOLLOUT
            }
            func(it.attachment(), opt)
        }
        return count > 0
    }

    override fun close() {
        native.close()
    }

    actual class Key : Closeable {
        actual var mode: Int
            get() = TODO("Not yet implemented")
            set(value) {}
        actual val attachment: Any?
            get() = TODO("Not yet implemented")

        override fun close() {
            TODO("Not yet implemented")
        }
    }
}