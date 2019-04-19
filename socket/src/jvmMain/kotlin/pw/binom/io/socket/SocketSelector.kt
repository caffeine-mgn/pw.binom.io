package pw.binom.io.socket

import pw.binom.io.Closeable
import java.nio.channels.Selector
import java.nio.channels.IllegalBlockingModeException as JIllegalBlockingModeException
import java.nio.channels.SelectionKey as JSelectionKey

actual class SocketSelector actual constructor(connections: Int) : Closeable {
    override fun close() {
        native.close()
    }

    private class SelectorKeyImpl(override val channel: Channel, override val attachment: Any?) : SelectorKey {
        override fun cancel() {
            key.cancel()
        }

        lateinit var key: JSelectionKey
    }

    private val native = Selector.open()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {
        val ss = SelectorKeyImpl(channel, attachment)
        val key = when (channel) {
            is SocketChannel -> try {
                channel.native.register(native, JSelectionKey.OP_READ, ss)
            } catch (e: JIllegalBlockingModeException) {
                throw IllegalBlockingModeException()
            }
            is ServerSocketChannel -> try {
                channel.native.register(native, JSelectionKey.OP_ACCEPT, ss)
            } catch (e: JIllegalBlockingModeException) {
                throw IllegalBlockingModeException()
            }
            else -> TODO()
        }
        ss.key = key
        return ss
    }

//    actual fun unreg(channel: Channel) {
//        val native = when (channel) {
//            is SocketChannel -> channel.native
//            is ServerSocketChannel -> channel.native
//            else -> TODO()
//        }
//        this.native.keys().find { it.channel() === native }?.cancel()
//    }

    actual fun process(func: (SelectorKey) -> Unit): Boolean {
        if (native.select() <= 0) {
            return false
        }

        val itt = native.selectedKeys().iterator()
        itt.forEach {
            try {
                val key = it.attachment() as SelectorKeyImpl
                func(key)
            } finally {
                itt.remove()
            }
        }
        return true
    }

    actual interface SelectorKey {
        actual val channel: Channel
        actual val attachment: Any?
        actual fun cancel()
    }

}