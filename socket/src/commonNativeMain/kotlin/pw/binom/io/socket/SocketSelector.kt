package pw.binom.io.socket

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.concurrency.asReference
import pw.binom.doFreeze
import pw.binom.io.Closeable

private const val LISTEN_READ = 0b01
private const val LISTEN_WRITE = 0b10

actual class SocketSelector actual constructor() : Closeable {

    private var closed = false

    private val native = NativeEpoll(1024)
    private val list = NativeEpollList(1024)
    override fun close() {
        if (closed)
            throw IllegalStateException("SocketSelector already closed")
        list.free()
        native.free()
        closed = true
    }

    internal inner class SelectorKeyImpl(override val channel: NetworkChannel,
                                         attachment: Any?) : SelectorKey {

        private val _attachment = attachment.asReference()

        override val attachment: Any?
            get() = _attachment.value

        private var _canlelled by AtomicBoolean(false)

        lateinit var selfRef: SelfRefKey

        override val isCanlelled: Boolean
            get() = _canlelled

        private var listenState by AtomicInt(0)

        override val listenReadable
            get() = listenState and LISTEN_READ != 0

        override val listenWritable
            get() = listenState and LISTEN_WRITE != 0

        internal var state by AtomicInt(0)

        override val isReadable
            get() = state and LISTEN_READ != 0

        override val isWritable
            get() = state and LISTEN_WRITE != 0

        override fun cancel() {
            if (isCanlelled) {
                throw IllegalStateException("SocketKey already cancelled")
            }
            native.remove(channel.nsocket.native)
            selfRef.close()
            _canlelled = true
//            _keys -= this
        }

        override fun listen(read: Boolean, write: Boolean) {
            if (listenReadable == read && listenWritable == write) {
                return
            }
            var status = 0
            if (read) {
                status = status or LISTEN_READ
            }
            if (write) {
                status = status or LISTEN_WRITE
            }
            listenState = status
            native.edit(channel.nsocket.native, selfRef, read, write)
        }
    }

//    private val _keys = HashSet<SelectorKeyImpl>()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {
        channel as NetworkChannel


        val key = SelectorKeyImpl(channel, attachment)


        if (channel.nsocket.blocking)
            throw IllegalBlockingModeException()

        val ref = native.add(channel.nsocket.native, key)
        key.selfRef = ref
//        _keys += key
        return key
    }

    actual fun process(timeout: Int?, func: (SelectorKey) -> Unit): Boolean {
        val count = native.wait(list, 1024, timeout ?: -1)
        if (count <= 0) {
            return false
        }
        for (i in 0 until count) {
            val item = list[i]
            val el = item.key
            var s = 0
            if (item.isReadable)
                s = s or LISTEN_READ
            if (item.isWritable)
                s = s or LISTEN_WRITE
            el.state = s
            if (item.isClosed) {
                val cc = el.channel
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
        get() = emptySet()//_keys

    actual interface SelectorKey {
        actual val channel: Channel
        actual val attachment: Any?
        actual val isReadable: Boolean
        actual val isWritable: Boolean
        actual val listenReadable: Boolean
        actual val listenWritable: Boolean
        actual val isCanlelled: Boolean
        actual fun cancel()
        actual fun listen(read: Boolean, write: Boolean)
    }

    init {
        doFreeze()
    }
}