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
    }

    internal inner class SelectorKeyImpl(override val channel: NetworkChannel,
                                         override val attachment: Any?) : SelectorKey {

        private var _canlelled = false
        lateinit var event: NativeEvent
        lateinit var selfRef: SelfRefKey

        override val isCanlelled: Boolean
            get() = _canlelled

        override var listenReadable: Boolean = false
            private set
        override var listenWritable: Boolean = false
            private set
        override var isReadable: Boolean = false
        override var isWritable: Boolean = false

        override fun cancel() {
            if (isCanlelled)
                throw IllegalStateException("SocketKey already cancelled")
            native.remove(channel.nsocket.native)
            selfRef.close()
            _canlelled = true
            _keys -= this
        }

        override fun updateListening(read: Boolean, write: Boolean) {
            if (listenReadable == read && listenWritable == write)
                return
            listenReadable = read
            listenWritable = write
            println("update key: read: [$read], write: [$write]")
            native.edit(channel.nsocket.native, selfRef, read, write)
        }
    }

    private val _keys = HashSet<SelectorKeyImpl>()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {
        channel as NetworkChannel


        val key = SelectorKeyImpl(channel, attachment)


        if (channel.nsocket.blocking)
            throw IllegalBlockingModeException()

        val ref = native.add(channel.nsocket.native, key)
        key.selfRef = ref
        _keys += key
        return key
    }

    actual fun process(timeout: Int?, func: (SelectorKey) -> Unit): Boolean {
        val count = native.wait(list, connections, timeout ?: -1)
        if (count <= 0)
            return false
        for (i in 0 until count) {
            val item = list[i]
            val el = item.key
            el.isReadable = item.isReadable
            el.isWritable = item.isWritable
            if (item.isClosed) {
                val cc=el.channel
                when (cc) {
                    is RawSocketChannel -> cc.socket.internalDisconnected()
                    is RawServerSocketChannel -> el.channel.nsocket.internalDisconnected()
                }
//                when  {
//                    isClient(cc) -> cc.socket.internalDisconnected()
//                    isServer(cc) -> el.channel.nsocket.internalDisconnected()
//                }
            }
            func(el)
        }
        return true
    }

    actual val keys: Collection<SelectorKey>
        get() = _keys

    actual interface SelectorKey {
        actual val channel: Channel
        actual val attachment: Any?
        actual val isReadable: Boolean
        actual val isWritable: Boolean
        actual val listenReadable: Boolean
        actual val listenWritable: Boolean
        actual val isCanlelled: Boolean
        actual fun cancel()
        actual fun updateListening(read: Boolean, write: Boolean)
    }

}