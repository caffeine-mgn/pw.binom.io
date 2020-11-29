package pw.binom.network

import java.net.ConnectException
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.channels.Selector as JSelector

private fun javaToCommon(mode: Int): Int {
    var opts = 0
    if (SelectionKey.OP_ACCEPT and mode != 0 || SelectionKey.OP_READ and mode != 0) {
        opts = opts or Selector.EVENT_EPOLLIN
    }

    if (SelectionKey.OP_WRITE and mode != 0) {
        opts = opts or Selector.EVENT_EPOLLOUT
    }
    return opts
}

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

class JvmSelector : Selector {
    private val native = JSelector.open()

    class JvmKey(
        override val attachment: Any?
    ) : Selector.Key {
        lateinit var native: SelectionKey
        override var listensFlag: Int
            get() = javaToCommon(native.interestOps())
            set(value) {
                native.interestOps(commonToJava(native.channel() as SocketChannel, value))
            }
        override val eventsFlag: Int
            get() = javaToCommon(native.readyOps())

        override fun close() {
            native.cancel()
        }
    }

    override fun select(timeout: Long, func: (Selector.Key, mode: Int) -> Unit): Int {
        when {
            timeout > 0L -> native.select(timeout)
            timeout == 0L -> native.selectNow()
            timeout < 0L -> native.select()
        }
        println("---===SELECT===---")
        val keys = native.selectedKeys()
        var count = 0
        keys.forEach {
            println("CHANNEL=${it.channel().hashCode()} event: ${jvmModeToString(it.readyOps())}, listen: ${jvmModeToString(it.interestOps())}")
            if (it.isConnectable) {
                val cc = it.channel() as SocketChannel
                if (cc.isConnectionPending) {
                    try {
                        println("cc.isConnected=${cc.isConnected}")
                        val connected = cc.finishConnect()
                        println("-->finishConnect=$connected   cc.isConnected=${cc.isConnected}")
                        if (connected) {
                            it.interestOps((it.interestOps().inv() or SelectionKey.OP_CONNECT).inv())
                            println("After connect: ${jvmModeToString(it.interestOps())}")
                            count++
                            func(it.attachment() as JvmKey, Selector.EVENT_CONNECTED or Selector.EVENT_EPOLLOUT)
                            return@forEach
                        } else {
                            return@forEach
                        }
                    } catch (e: ConnectException) {
                        count++
                        func(it.attachment() as JvmKey, Selector.EVENT_ERROR)
                        return@forEach
                    }
                }
                if (it.isConnectable) {
                    return@forEach
                }
            }
            println("it.isAcceptable->${it.isAcceptable}")
            println("it.isConnectable->${it.isConnectable}")
            println("it.isReadable->${it.isReadable}")
            println("it.isWritable->${it.isWritable}")
            println("it.isValid->${it.isValid}")
            println("isConnected->${(it.channel() as SocketChannel).isConnected}")
            println("isConnectionPending->${(it.channel() as SocketChannel).isConnectionPending}")
            count++
            func(it.attachment() as JvmKey, javaToCommon(it.readyOps()))
        }
        return count
    }

    override fun attach(socket: TcpClientSocketChannel, mode: Int, attachment: Any?): Selector.Key {
        val key = JvmKey(attachment)
        val nn = socket.native.register(native, commonToJava(socket.native, mode) or SelectionKey.OP_CONNECT, key)
        key.native = nn
        return key
    }

    override fun attach(socket: TcpServerSocketChannel, mode: Int, attachment: Any?): Selector.Key {
        TODO("Not yet implemented")
    }

    override fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?): Selector.Key {
        TODO("Not yet implemented")
    }
}

actual fun createSelector(): Selector = JvmSelector()

fun jvmModeToString(mode: Int): String {
    val sb = StringBuilder()
    if (SelectionKey.OP_CONNECT and mode != 0) {
        sb.append("OP_CONNECT ")
    }
    if (SelectionKey.OP_ACCEPT and mode != 0) {
        sb.append("OP_ACCEPT ")
    }
    if (SelectionKey.OP_READ and mode != 0) {
        sb.append("OP_READ ")
    }
    if (SelectionKey.OP_WRITE and mode != 0) {
        sb.append("OP_WRITE ")
    }
    return sb.toString()
}