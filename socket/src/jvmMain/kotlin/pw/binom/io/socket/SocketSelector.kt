package pw.binom.io.socket

import pw.binom.collection.MappedCollection
import pw.binom.io.Closeable
import java.nio.channels.Selector
import java.nio.channels.IllegalBlockingModeException as JIllegalBlockingModeException
import java.nio.channels.SelectionKey as JSelectionKey

actual class SocketSelector actual constructor() : Closeable {
    override fun close() {
        native.close()
    }

    private val cancelledKeys = HashMap<Channel, HashSet<SelectorKeyImpl>>()

    private inner class SelectorKeyImpl(override val channel: Channel, override var attachment: Any?) : SelectorKey {

        private var _cancelled = false

        override val isCanlelled: Boolean
            get() = _cancelled

        override fun updateListening(read: Boolean, write: Boolean) {
            if (listenReadable == read && listenWritable == write) {
                return
            }
            var i = 0
            if (read)
                i = i or JSelectionKey.OP_READ
            if (write)
                i = i or JSelectionKey.OP_WRITE
            listenReadable = read
            listenWritable = write
            key.interestOps(i)
        }

        override var listenReadable: Boolean = false
        override var listenWritable: Boolean = false
        override var isReadable: Boolean = false
        override var isWritable: Boolean = false

        override fun cancel() {
            if (isCanlelled)
                throw IllegalStateException("SocketKey already cancelled")
            cancelledKeys.getOrPut(channel) { HashSet() }.add(this)
            listenReadable = false
            listenWritable = false
            _cancelled = true
        }

        fun rereg() {
            _cancelled = false
            listenWritable = false
            listenReadable = false
            cancelledKeys[channel]?.remove(this)
            if (cancelledKeys[channel]?.isEmpty() == true) {
                cancelledKeys.remove(channel)
            }
        }

        lateinit var key: JSelectionKey
    }

    private val native = Selector.open()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {
        val cancelled = cancelledKeys[channel]?.firstOrNull() as SelectorKeyImpl?
        if (cancelled != null) {
            cancelled.rereg()
            cancelled.attachment = attachment
            return cancelled
        }
        val ss = SelectorKeyImpl(channel, attachment)
        val key = when (channel) {
            is SocketChannel -> try {
                channel.native.register(native, /*JSelectionKey.OP_READ or JSelectionKey.OP_WRITE*/0, ss)
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
        while (itt.hasNext()) {
            val key1 = itt.next()
            itt.remove()
            val key = key1.attachment() as SelectorKeyImpl
            key.isReadable = key1.isReadable
            key.isWritable = key1.isWritable
            func(key)
        }
//        itt.forEach {
//            try {
//                val key = it.attachment() as SelectorKeyImpl
//                key.isReadable = it.isReadable
//                key.isWritable = it.isWritable
//                func(key)
//            } finally {
//                itt.remove()
//            }
//        }
        return true
    }

    actual interface SelectorKey {
        actual val channel: Channel
        actual val attachment: Any?
        actual fun cancel()
        actual val isReadable: Boolean
        actual val isWritable: Boolean
        actual val listenReadable: Boolean
        actual val listenWritable: Boolean
        actual val isCanlelled: Boolean
        actual fun listen(read: Boolean, write: Boolean)
    }

    actual val keys: Collection<SelectorKey>
        get() = MappedCollection(native.keys()) { it.attachment() as SelectorKey }

}