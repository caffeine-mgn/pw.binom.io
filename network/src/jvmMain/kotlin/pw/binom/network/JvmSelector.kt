package pw.binom.network

import java.net.ConnectException
import java.nio.channels.*
import java.nio.channels.Selector as JSelector

private fun javaToCommon(mode: Int): Int {
    var opts = 0
    if (SelectionKey.OP_ACCEPT and mode != 0 || SelectionKey.OP_READ and mode != 0) {
        opts = opts or Selector.INPUT_READY
    }

    if (SelectionKey.OP_WRITE and mode != 0) {
        opts = opts or Selector.OUTPUT_READY
    }
    return opts
}

private fun commonToJava(channel: NetworkChannel, mode: Int): Int {
    var opts = 0
    if (Selector.INPUT_READY and mode != 0) {
        val value = when (channel) {
            is SocketChannel, is DatagramChannel -> SelectionKey.OP_READ
            is ServerSocketChannel -> SelectionKey.OP_ACCEPT
            else -> throw IllegalArgumentException("Unsupported NetworkChannel: ${channel::class.java}")
        }
        opts = opts or value
    }

    if (Selector.OUTPUT_READY and mode != 0) {
        require(channel is SocketChannel || channel is DatagramChannel)
        opts = opts or SelectionKey.OP_WRITE
    }
    return opts
}

private fun commonToJava(channel: ServerSocketChannel, mode: Int): Int {
    var opts = 0
    if (Selector.INPUT_READY and mode != 0) {
        opts or SelectionKey.OP_ACCEPT
    }

    if (Selector.OUTPUT_READY and mode != 0) {
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
                if (native.interestOps() != value) {
                    native.interestOps(commonToJava(native.channel() as NetworkChannel, value))
                }
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
        val keys = native.selectedKeys()
        var count = 0
        keys.forEach {
            if (it.isConnectable) {
                val cc = it.channel() as SocketChannel
                if (cc.isConnectionPending) {
                    try {
                        val connected = cc.finishConnect()
                        if (connected) {
                            it.interestOps((it.interestOps().inv() or SelectionKey.OP_CONNECT).inv())
                            count++
                            func(it.attachment() as JvmKey, Selector.EVENT_CONNECTED or Selector.OUTPUT_READY)
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
        val key = JvmKey(attachment)
        val nn = socket.native.register(native, commonToJava(socket.native, mode), key)
        key.native = nn
        return key
    }

    override fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?): Selector.Key {
        val key = JvmKey(attachment)
        val nn = socket.native.register(native, commonToJava(socket.native, mode), key)
        key.native = nn
        return key
    }

    override fun close() {
        native.close()
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