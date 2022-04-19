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

        override fun hasNext(): Boolean = keys.hasNext()

        override fun next(): Selector.KeyEvent {
            val it = keys.next()
            if (it.isConnectable) {
                val socketChannel = it.channel() as SocketChannel
                try {
                    val connected = socketChannel.finishConnect()
                    if (connected) {
                        keyEvent.key = it.attachment() as JvmSelector.JvmKey
                        keyEvent.mode = Selector.EVENT_CONNECTED or Selector.OUTPUT_READY
                        if (it.isValid && it.interestOps() and SelectionKey.OP_CONNECT != 0) {
                            it.interestOps((it.interestOps().inv() or SelectionKey.OP_CONNECT).inv())
                        }
                        return keyEvent
                    } else {
                        keyEvent.mode = 0
                    }
                } catch (e: ConnectException) {
                    keyEvent.key = it.attachment() as JvmSelector.JvmKey
                    keyEvent.mode = Selector.EVENT_ERROR
                    return keyEvent
                } catch (e: SocketException) {
                    keyEvent.key = it.attachment() as JvmSelector.JvmKey
                    keyEvent.mode = Selector.EVENT_ERROR
                    return keyEvent
                }
            }
            keyEvent.key = it.attachment() as JvmSelector.JvmKey
            keyEvent.mode = javaToCommon(it.readyOps())
            return keyEvent
        }
    }
}
