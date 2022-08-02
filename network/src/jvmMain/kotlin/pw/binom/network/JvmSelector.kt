package pw.binom.network

import pw.binom.io.ClosedException
import java.nio.channels.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import java.nio.channels.Selector as JSelector

internal fun javaToCommon(mode: Int): Int {
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

    inner class JvmKey(
        override val attachment: Any?
    ) : Selector.Key {
        lateinit var native: SelectionKey

        private var _closed = false

        override val closed: Boolean
            get() {
                if (!_closed) {
                    if (!native.isValid) {
                        _closed = true
                        return true
                    }
                    return false
                }
                return true
            }

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
                opts = opts or SelectionKey.OP_CONNECT or SelectionKey.OP_READ
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
                native.selector().wakeup()
            }

        override fun close() {
            checkClosed()
            _closed = true
            try {
                native.interestOps(0)
            } catch (e: CancelledKeyException) {
                throw ClosedException()
            }
        }
    }

    override fun wakeup() {
        native.wakeup()
    }

    override fun getAttachedKeys(): Collection<Selector.Key> {
        return this.native.keys().mapNotNull {
            val key = it.attachment() as JvmKey
            if (key.closed || !key.native.isValid) {
                null
            } else {
                key
            }
        }
    }

    private val lock = ReentrantLock()

    @OptIn(ExperimentalTime::class)
    override fun select(timeout: Long, selectedEvents: SelectedEvents): Int {
        lock.lock()
        selectedEvents.lock.lock()
        try {
            native.selectedKeys().clear()
            val eventCount = when {
                timeout > 0L -> {
                    var selectedCount = 0
                    val selectTime = measureTime {
                        selectedCount = native.select(timeout)
                    }
                    if (selectedCount == 0 && selectTime.inWholeMilliseconds < timeout) {
                        native.select(timeout - selectTime.inWholeMilliseconds)
                    } else {
                        selectedCount
                    }
                }
                else -> native.select()
            }
            val keys = HashSet(native.selectedKeys())
            selectedEvents.selectedKeys = keys
            return keys.size
        } finally {
            selectedEvents.lock.unlock()
            lock.unlock()
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

internal fun jvmModeToString(mode: Int): String {
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
