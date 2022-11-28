package pw.binom.network

import pw.binom.collections.defaultMutableSet
import java.nio.channels.*
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import java.nio.channels.Selector as JSelector

internal fun javaToCommon(mode: Int): Int {
    var opts = 0
    if (SelectionKey.OP_ACCEPT in mode || SelectionKey.OP_READ in mode) {
        opts = opts or Selector.INPUT_READY
    }

    if (SelectionKey.OP_WRITE in mode) {
        opts = opts or Selector.OUTPUT_READY
    }

    if (SelectionKey.OP_CONNECT in mode) {
        opts = opts or Selector.EVENT_CONNECTED
    }
    return opts
}

private operator fun Int.contains(opConnect: Int): Boolean = this and opConnect != 0

class JvmSelector : Selector {
    internal val native = JSelector.open()

    @Volatile
    private var selecting = false

    internal var keysNotInSelector = defaultMutableSet<JvmKey>()

    override fun wakeup() {
        native.wakeup()
    }

    override fun getAttachedKeys(): Collection<Selector.Key> {
        return this.native.keys().mapNotNull {
            val key = it.attachment() as JvmKey
            val native = key.native ?: return@mapNotNull null
            if (key.closed || !native.isValid) {
                null
            } else {
                key
            }
        } + keysNotInSelector
    }

    private val lock = ReentrantLock()

    @OptIn(ExperimentalTime::class)
    override fun select(selectedEvents: SelectedEvents, timeout: Long): Int {
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
            keys.forEach {
                (it.attachment() as JvmKey).internalResetKey()
            }
            selectedEvents.selectedKeys = keys
            return keys.size
        } finally {
            selectedEvents.lock.unlock()
            lock.unlock()
        }
    }

    override fun attach(socket: TcpClientSocketChannel, attachment: Any?, mode: Int): Selector.Key {
        val key = JvmKey(attachment, initMode = mode, connected = false, selector = this)
        keysNotInSelector += key
        socket.key = key
//            native.wakeup()
//            val nn = socket.native!!.register(native, key.commonToJava(socket.native!!, mode), key)
//            key.native = nn
        return key
    }

    override fun attach(socket: TcpServerSocketChannel, attachment: Any?, mode: Int): Selector.Key {
        val key = JvmKey(attachment, initMode = mode, connected = true, selector = this)
        keysNotInSelector += key
        socket.key = key
//        val nn = socket.native!!.register(native, key.commonToJava(socket.native!!, mode), key)
//        key.native = nn
//        native.wakeup()
        return key
    }

    override fun attach(socket: UdpSocketChannel, attachment: Any?, mode: Int): Selector.Key {
        val key = JvmKey(attachment, initMode = mode, connected = true, selector = this)
        key.setNative(socket.native)
//        socket.key = key
//        val nn = socket.native.register(native, key.commonToJava(socket.native, mode), key)
//        key.native = nn
//        native.wakeup()
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
