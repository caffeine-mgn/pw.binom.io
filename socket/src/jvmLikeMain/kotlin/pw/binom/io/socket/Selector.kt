package pw.binom.io.socket

import pw.binom.io.Closeable
import java.nio.channels.SocketChannel
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration
import java.nio.channels.Selector as JvmSelector

actual class Selector : Closeable {
    private val native = JvmSelector.open()
    private val lock = ReentrantLock()
    actual fun attach(socket: Socket): SelectorKey {
        if (socket.blocking) {
            throw IllegalArgumentException("Socket in blocking mode")
        }
        val existKey = socket.native.keyFor(native)
        if (existKey != null) {
            return existKey.attachment() as SelectorKey
        }
        val jvmKey = socket.native.register(native, 0, null)
        val binomKey = SelectorKey(native = jvmKey, selector = this)
        jvmKey.attach(binomKey)
        return binomKey
    }

    actual fun select(timeout: Duration, selectedKeys: SelectedKeys) {
        lock.withLock {
            selectedKeys.lock.withLock {
                native.selectedKeys().clear()
                selectedKeys.errors.clear()
                when {
                    timeout.isInfinite() -> native.select()
                    timeout == Duration.ZERO -> native.selectNow()
                    else -> native.select(timeout.inWholeMilliseconds)
                }
                val list = ArrayList(native.selectedKeys())
                list.forEach {
                    val binomKey = it.attachment() as SelectorKey
                    if (it.isConnectable) {
                        val channel = it.channel() as? SocketChannel
                        if (channel?.isConnectionPending == true) {
                            try {
                                channel.finishConnect()
                            } catch (e: java.net.ConnectException) {
                                binomKey.listenFlags = 0
                                selectedKeys.errors += binomKey
                                return@forEach
                                // Do nothing
                            }
                        }
                    }
                    binomKey.listenFlags = 0
                }
                selectedKeys.selectedKeys = list
            }
        }
    }

    actual fun wakeup() {
        native.wakeup()
    }

    override fun close() {
        native.close()
    }
}
