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

    private val eventImpl = object : pw.binom.io.socket.Event {
        var internalKey: SelectorKey? = null
        var internalFlag: Int = 0
        override val key: SelectorKey
            get() = internalKey ?: throw IllegalStateException("key not set")
        override val flags: Int
            get() = internalFlag

        override fun toString(): String = this.buildToString()
    }

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

    private fun select(timeout: Duration): Int {
        native.selectedKeys().clear()
        return when {
            timeout.isInfinite() -> native.select()
            timeout == Duration.ZERO -> native.selectNow()
            else -> native.select(timeout.inWholeMilliseconds)
        }
    }

    actual fun select(timeout: Duration, selectedKeys: SelectedKeys) {
        lock.withLock {
            selectedKeys.lock.withLock {
                selectedKeys.errors.clear()
                select(timeout = timeout)
                val list = ArrayList(native.selectedKeys())
                list.forEach {
                    val binomKey = it.attachment() as SelectorKey
                    if (it.isConnectable) {
                        val channel = it.channel() as? SocketChannel
                        if (channel?.isConnectionPending == true) {
                            try {
                                channel.finishConnect()
                                binomKey.isErrorHappened = false
                            } catch (e: java.net.ConnectException) {
                                binomKey.isErrorHappened = true
                                if (binomKey.listenFlags and KeyListenFlags.ONCE != 0) {
                                    binomKey.listenFlags = 0
                                }
                                selectedKeys.errors += binomKey
                                return@forEach
                            }
                        }
                    }
                    binomKey.isErrorHappened = false
                    if (binomKey.listenFlags and KeyListenFlags.ONCE != 0) {
                        binomKey.listenFlags = 0
                    }
                }
                selectedKeys.selectedKeys = list
            }
        }
    }

    actual fun select(timeout: Duration, eventFunc: (pw.binom.io.socket.Event) -> Unit) {
        lock.withLock {
            select(timeout = timeout)
            native.selectedKeys().forEach { nativeKey ->
                val binomKey = nativeKey.attachment() as SelectorKey
                eventImpl.internalKey = binomKey
                when {
                    !nativeKey.isValid -> {
                        binomKey.isErrorHappened = false
                        eventImpl.internalFlag = KeyListenFlags.ERROR or KeyListenFlags.READ or KeyListenFlags.WRITE
                        eventFunc(eventImpl)
                        return@forEach
                    }

                    nativeKey.isConnectable -> {
                        val channel = nativeKey.channel() as? SocketChannel
                        if (channel?.isConnectionPending == true) {
                            try {
                                channel.finishConnect()
                                binomKey.isErrorHappened = false
                                eventImpl.internalFlag = KeyListenFlags.WRITE or nativeKey.toCommonReadFlag()
                                eventFunc(eventImpl)
                            } catch (e: java.net.ConnectException) {
                                binomKey.isErrorHappened = true
                                if (binomKey.listenFlags and KeyListenFlags.ONCE != 0) {
                                    binomKey.listenFlags = 0
                                }
                                eventImpl.internalFlag = KeyListenFlags.ERROR or KeyListenFlags.READ
                                eventFunc(eventImpl)
                                return@forEach
                            }
                        }
                    }

                    else -> {
                        eventImpl.internalFlag = nativeKey.toCommonReadFlag()
                        eventFunc(eventImpl)
                    }
                }
                binomKey.isErrorHappened = false
                if (binomKey.listenFlags and KeyListenFlags.ONCE != 0) {
                    binomKey.listenFlags = 0
                }
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
