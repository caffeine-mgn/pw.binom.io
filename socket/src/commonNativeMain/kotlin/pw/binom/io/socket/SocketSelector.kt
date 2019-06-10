package pw.binom.io.socket

import pw.binom.io.Closeable
import pw.binom.neverFreeze

actual class SocketSelector actual constructor(private val connections: Int) : Closeable {

    init {
        neverFreeze()
    }

    private val native = NativeEpoll(connections)
    private val list = NativeEpollList(connections)
    override fun close() {
        list.free()
        native.free()
        elements.clear()
    }

    private inner class SelectorKeyImpl(override val channel: NetworkChannel, override val attachment: Any?, r: Boolean, w: Boolean) : SelectorKey {

        private var _canlelled = false

        override val isCanlelled: Boolean
            get() = _canlelled

        override var listenReadable: Boolean = r
            set(value) {
                if (value == field)
                    return
                field = value
                native.edit(channel.nsocket.native, value, listenWritable)
            }
        override var listenWritable: Boolean = w
            set(value) {
                if (value == field)
                    return
                field = value
                native.edit(channel.nsocket.native, listenReadable, value)
            }
        override var isReadable: Boolean = false
        override var isWritable: Boolean = false

        override fun cancel() {
            if (isCanlelled)
                throw IllegalStateException("SocketKey already cancelled")
            elements.remove(channel.nsocket.native.code)
            native.remove(channel.nsocket.native)
            _canlelled = true
        }
    }

    private val elements = HashMap<Int, SelectorKeyImpl>()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {
        channel as NetworkChannel

        val key = SelectorKeyImpl(channel, attachment, channel is RawSocketChannel, channel is RawSocketChannel)


        if (channel.nsocket.blocking)
            throw IllegalBlockingModeException()

        native.add(channel.nsocket.native)
        elements[channel.nsocket.native.code] = key
        return key
    }

    actual fun process(timeout: Int?, func: (SelectorKey) -> Unit): Boolean {
        val count = native.wait(list, connections, timeout ?: -1)
        if (count <= 0)
            return false
        for (i in 0 until count) {
            val item = list[i]

            val el = elements[item.socId] ?: continue
            el.isReadable = item.isReadable
            el.isWritable = item.isWritable
            if (item.isClosed) {
                when (el.channel) {
                    is RawSocketChannel -> el.channel.socket.internalDisconnected()
                    is RawServerSocketChannel -> el.channel.nsocket.internalDisconnected()
                }
//                el.cancel()
            }
            func(el)
        }
        return true
    }

    actual val keys: Collection<SelectorKey>
        get() = elements.values

    actual interface SelectorKey {
        actual val channel: Channel
        actual val attachment: Any?
        actual val isReadable: Boolean
        actual val isWritable: Boolean
        actual var listenReadable: Boolean
        actual var listenWritable: Boolean
        actual val isCanlelled: Boolean
        actual fun cancel()
    }

}