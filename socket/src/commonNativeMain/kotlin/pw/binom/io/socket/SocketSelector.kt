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
        override var listenReadable: Boolean = r
            set(value) {
                if (value == field)
                    return
                field = value
                native.edit(channel.socket.native, value, listenWritable)
            }
        override var listenWritable: Boolean = w
            set(value) {
                if (value == field)
                    return
                field = value
                native.edit(channel.socket.native, listenReadable, value)
            }
        override var isReadable: Boolean = false
        override var isWritable: Boolean = false

        override fun cancel() {
            elements.remove(channel.socket.native.code)
            native.remove(channel.socket.native)
        }
    }

    private val elements = HashMap<Int, SelectorKeyImpl>()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {
        channel as NetworkChannel

        val key = SelectorKeyImpl(channel, attachment, channel is SocketChannel, channel is SocketChannel)


        if (channel.socket.blocking)
            throw IllegalBlockingModeException()

        native.add(channel.socket.native)
        elements[channel.socket.native.code] = key
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
                    is SocketChannel -> el.channel.socket.internalDisconnected()
                    is ServerSocketChannel -> el.channel.socket.internalDisconnected()
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
        actual fun cancel()
    }

}