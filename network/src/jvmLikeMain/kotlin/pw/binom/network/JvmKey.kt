package pw.binom.network

import pw.binom.io.ClosedException
import java.nio.channels.*
import java.nio.channels.spi.AbstractSelectableChannel

class JvmKey(
    override val attachment: Any?,
    private var initMode: Int,
    var connected: Boolean,
    override val selector: JvmSelector
) : SelectorOld.Key {
    var native: SelectionKey? = null
        private set

    private var _closed = false

    init {
        NetworkMetrics.incSelectorKey()
    }

    fun setNative(native: AbstractSelectableChannel) {
        selector.keysNotInSelector -= this
        this.native = native.register(selector.native, commonToJava(native, initMode), this)
    }

    override val closed: Boolean
        get() {
            if (!_closed) {
                val native = native
                if (native != null && !native.isValid) {
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
        if (SelectorOld.INPUT_READY and mode != 0) {
            val value = when (channel) {
                is SocketChannel, is DatagramChannel -> SelectionKey.OP_READ
                is ServerSocketChannel -> SelectionKey.OP_ACCEPT
                else -> throw IllegalArgumentException("Unsupported NetworkChannel: ${channel::class.java}")
            }
            opts = opts or value
        }
        if (SelectorOld.OUTPUT_READY and mode != 0) {
            require(channel is SocketChannel || channel is DatagramChannel)
            opts = opts or SelectionKey.OP_WRITE
        }
        if (SelectorOld.EVENT_CONNECTED and mode != 0) {
            require(channel is SocketChannel)
            opts = opts or SelectionKey.OP_CONNECT or SelectionKey.OP_READ or SelectionKey.OP_WRITE
        }
//            if (Selector.EVENT_ERROR and mode != 0) {
//                opts = opts or SelectionKey.OP_WRITE
//            }
        return opts
    }

    internal fun internalResetKey() {
        initMode = 0
        native?.interestOps(0)
    }

    override var listensFlag: Int
        get() = initMode
        set(value) {
            checkClosed()
            initMode = value
            native?.let { native ->
                val javaOps = commonToJava(native.channel(), value)
                native.interestOps(javaOps)
            }
        }

    override fun close() {
        checkClosed()
        _closed = true
        NetworkMetrics.decSelectorKey()
        try {
            native?.interestOps(0)
        } catch (e: CancelledKeyException) {
            throw ClosedException()
        }
    }

    override fun toString(): String {
        val nativeMode = jvmModeToString(native?.takeIf { it.isValid }?.interestOps() ?: 0)
        return "JvmSelector.JvmKey(native=$nativeMode, ${generateToString()})"
    }
}
