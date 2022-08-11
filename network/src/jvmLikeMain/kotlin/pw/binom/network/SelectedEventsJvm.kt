package pw.binom.network

import java.net.ConnectException
import java.net.SocketException
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.util.concurrent.locks.ReentrantLock

class SelectedEventsJvm : SelectedEvents {

    override var selectedKeys: Set<SelectionKey> = emptySet()
    override val lock = ReentrantLock()

    override fun close() {
    }

    override fun iterator(): Iterator<Selector.KeyEvent> {
        selectorIterator.reset()
        return selectorIterator
    }

    private val keyEvent = object : Selector.KeyEvent {
        override lateinit var key: Selector.Key
        override var mode: Int = 0

        override fun toString(): String = selectorModeToString(mode)
    }

    private val selectorIterator = object : Iterator<Selector.KeyEvent> {
        private lateinit var keys: Iterator<SelectionKey>
        fun reset() {
            keys = selectedKeys.iterator()
        }

        private var nextReady = false

        override fun hasNext(): Boolean {
            if (nextReady) {
                return true
            }
            while (true) {
                if (!keys.hasNext()) {
                    return false
                }
                val it = keys.next()
                val key = it.attachment() as JvmSelector.JvmKey
                val socketChannel = it.channel() as? SocketChannel
                if (socketChannel != null && !key.connected && socketChannel.isConnected && it.isWritable) {
                    try {
                        keyEvent.key = key
                        keyEvent.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                        if (it.isValid && (it.isConnectable || it.isWritable)) {
                            it.interestOps(
                                (it.interestOps().inv() or SelectionKey.OP_CONNECT or Selector.OUTPUT_READY).inv()
                            )
                        }
                        nextReady = true
                        return true
                    } catch (e: ConnectException) {
                        keyEvent.key = it.attachment() as JvmSelector.JvmKey
                        keyEvent.mode = Selector.EVENT_ERROR
                        nextReady = true
                        return true
                    } catch (e: SocketException) {
                        keyEvent.key = it.attachment() as JvmSelector.JvmKey
                        keyEvent.mode = Selector.EVENT_ERROR
                        nextReady = true
                        return true
                    }
                }
                if (it.isConnectable) {
                    val socketChannel = it.channel() as SocketChannel
                    if (!socketChannel.isConnectionPending) {
                        continue
                    }
                    try {
                        val connected = socketChannel.finishConnect()
                        if (!socketChannel.isConnected) {
                            continue
                        }
                        if (connected) {
                            keyEvent.key = key
                            keyEvent.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                            if (it.isValid && (it.isConnectable || it.isWritable)) {
                                it.interestOps(
                                    (it.interestOps().inv() or SelectionKey.OP_CONNECT or SelectionKey.OP_WRITE).inv()
                                )
                            }
                            nextReady = true
                            key.connected = true
                            return true
                        } else {
                            keyEvent.mode = 0
                        }
                    } catch (e: ConnectException) {
                        keyEvent.key = it.attachment() as JvmSelector.JvmKey
                        keyEvent.mode = Selector.EVENT_ERROR
                        nextReady = true
                        return true
                    } catch (e: SocketException) {
                        keyEvent.key = it.attachment() as JvmSelector.JvmKey
                        keyEvent.mode = Selector.EVENT_ERROR
                        nextReady = true
                        return true
                    }
                }
                keyEvent.key = it.attachment() as JvmSelector.JvmKey
                keyEvent.mode = javaToCommon(it.readyOps())
                nextReady = true
                return true
            }
        }

        override fun next(): Selector.KeyEvent {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            nextReady = false
            return keyEvent
        }
    }
}
