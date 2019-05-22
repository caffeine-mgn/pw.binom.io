package pw.binom.io.socket

import pw.binom.collection.MappedCollection
import pw.binom.io.Closeable
import java.nio.channels.Selector
import java.nio.channels.IllegalBlockingModeException as JIllegalBlockingModeException
import java.nio.channels.SelectionKey as JSelectionKey

actual class SocketSelector actual constructor(connections: Int) : Closeable {
    override fun close() {
        native.close()
    }

    private class SelectorKeyImpl(override val channel: Channel, override val attachment: Any?) : SelectorKey {

        private var _canlelled = false

        override val isCanlelled: Boolean
            get() = _canlelled

        override var listenReadable: Boolean
            get() = key.interestOps() and JSelectionKey.OP_READ != 0
            set(value) {
                if (value == listenReadable)
                    return
                if (value)
                    key.interestOps(key.interestOps() or JSelectionKey.OP_READ)
                else
                    key.interestOps(key.interestOps() xor JSelectionKey.OP_READ)
            }
        override var listenWritable: Boolean
            get() = key.interestOps() and JSelectionKey.OP_WRITE != 0
            set(value) {
                if (value == listenWritable)
                    return
                if (value)
                    key.interestOps(key.interestOps() or JSelectionKey.OP_WRITE)
                else
                    key.interestOps(key.interestOps() xor JSelectionKey.OP_WRITE)
            }
        override var isReadable: Boolean = false
        override var isWritable: Boolean = false

        override fun cancel() {
            if (isCanlelled)
                throw IllegalStateException("SocketKey already cancelled")
            key.cancel()
            _canlelled = true
        }

        lateinit var key: JSelectionKey
    }

    private val native = Selector.open()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {
        val ss = SelectorKeyImpl(channel, attachment)
        val key = when (channel) {
            is SocketChannel -> try {
                channel.native.register(native, JSelectionKey.OP_READ or JSelectionKey.OP_WRITE, ss)
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

    actual fun process(timeout: Int?, func: (SelectorKey) -> Unit): Boolean {
        val founded = if (timeout != null)
            native.select(timeout.toLong())
        else
            native.select()

        if (founded <= 0) {
            return false
        }
        val itt = native.selectedKeys().iterator()
        itt.forEach {
            try {
                val key = it.attachment() as SelectorKeyImpl
                key.isReadable = it.isReadable
                key.isWritable = it.isWritable
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
        actual val isReadable: Boolean
        actual val isWritable: Boolean
        actual var listenReadable: Boolean
        actual var listenWritable: Boolean
        actual val isCanlelled: Boolean
    }

    actual val keys: Collection<SelectorKey>
        get() = MappedCollection(native.keys()) { it.attachment() as SelectorKey }

}