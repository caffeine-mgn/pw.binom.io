package pw.binom.network

import pw.binom.concurrency.ThreadRef
import java.net.ConnectException
import java.net.SocketException
import java.nio.channels.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import java.nio.channels.Selector as JSelector

private fun javaToCommon(mode: Int): Int {
    var opts = 0
    if (SelectionKey.OP_ACCEPT and mode != 0 || SelectionKey.OP_READ and mode != 0) {
        opts = opts or Selector.INPUT_READY
    }

    if (SelectionKey.OP_WRITE and mode != 0) {
        opts = opts or Selector.OUTPUT_READY
    }

    if (SelectionKey.OP_CONNECT and mode != 0) {
        opts = opts or Selector.EVENT_CONNECTED
    }
    return opts
}

class JvmSelector : Selector {
    private val native = JSelector.open()

    @Volatile
    private var selecting = false
    private var networkThread = ThreadRef()

    inner class JvmKey(
        override val attachment: Any?
    ) : Selector.Key {
        lateinit var native: SelectionKey

        private var _closed = false

        override val closed: Boolean
            get() = _closed

        private inline fun checkClosed() {
            check(!_closed) { "SelectorKey already closed" }
        }

        fun commonToJava(channel: SelectableChannel, mode: Int): Int {
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
            if (Selector.EVENT_CONNECTED and mode != 0) {
                require(channel is SocketChannel)
                opts = opts or SelectionKey.OP_CONNECT
            }
            return opts
        }

        override var listensFlag: Int
            get() {
                checkClosed()
                return javaToCommon(native.interestOps())
            }
            set(value) {
                checkClosed()
                val javaOps = commonToJava(native.channel(), value)
                native.interestOps(javaOps)
                if (!networkThread.same) {
                    native.selector().wakeup()
                }
            }

        override fun close() {
            checkClosed()
            _closed = true
            native.interestOps(0)
//            native.cancel()
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun select(timeout: Long, func: (Selector.Key, mode: Int) -> Unit): Int {
        if (selecting) {
            throw IllegalStateException("Selector already trying select keys")
        }
        selecting = true
        try {
            val time = measureTime {
                val count2 = when {
                    timeout > 0L -> native.select(timeout)
                    timeout == 0L -> native.selectNow()
                    timeout < 0L -> native.select()
                    else -> throw IllegalArgumentException("Invalid timeout $timeout")
                }
            }
            val keys = native.selectedKeys()
            var count = 0
            val iterator = keys.iterator()
            while (iterator.hasNext()) {
                val it = iterator.next()
                iterator.remove()
                if (it.isConnectable) {
                    val cc = it.channel() as SocketChannel

                    try {
                        val connected = cc.finishConnect()
                        if (connected) {
                            count++
                            func(it.attachment() as JvmKey, Selector.EVENT_CONNECTED or Selector.OUTPUT_READY)
                            if (it.interestOps() and SelectionKey.OP_CONNECT != 0) {
                                it.interestOps((it.interestOps().inv() or SelectionKey.OP_CONNECT).inv())
                            }
                            continue
                        } else {
                            continue
                        }
                    } catch (e: ConnectException) {
                        count++
                        func(it.attachment() as JvmKey, Selector.EVENT_ERROR)
                        continue
                    } catch (e: SocketException){
                        count++
                        func(it.attachment() as JvmKey, Selector.EVENT_ERROR)
                        continue
                    }
//                }
//                if (it.isConnectable) {
//                    return@forEach
//                }
                }
                count++
                func(it.attachment() as JvmKey, javaToCommon(it.readyOps()))
            }
            native.selectedKeys().clear()
            return count
        } finally {
            selecting = false
        }
    }

    override fun attach(socket: TcpClientSocketChannel, mode: Int, attachment: Any?): Selector.Key {
        val key = JvmKey(attachment)
        val nn = socket.native.register(native, key.commonToJava(socket.native, mode), key)
        key.native = nn
        return key
    }

    override fun attach(socket: TcpServerSocketChannel, mode: Int, attachment: Any?): Selector.Key {
        val key = JvmKey(attachment)
        val nn = socket.native.register(native, key.commonToJava(socket.native, mode), key)
        key.native = nn
        return key
    }

    override fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?): Selector.Key {
        val key = JvmKey(attachment)
        val nn = socket.native.register(native, key.commonToJava(socket.native, mode), key)
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