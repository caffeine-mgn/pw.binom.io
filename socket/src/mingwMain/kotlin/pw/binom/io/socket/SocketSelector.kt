package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.*
import platform.linux.SOCKET
import platform.linux.epoll_event
import platform.posix.free
import platform.posix.malloc
import pw.binom.io.Closeable
import kotlin.native.concurrent.ensureNeverFrozen

actual class SocketSelector actual constructor(private val connections: Int) : Closeable {

    init {
        this.ensureNeverFrozen()
    }

    private val native = epoll_create(connections)
    private val list = malloc((sizeOf<epoll_event>() * connections).convert())!!.reinterpret<epoll_event>()
    override fun close() {
        free(list)
        epoll_close(native)
        elements.clear()
    }

    private inner class SelectorKeyImpl(override val channel: NetworkChannel, override val attachment: Any?) : SelectorKey {
        override fun cancel() {
            channel.unregSelector(this@SocketSelector)
            elements.remove(channel.socket.native)
            epoll_ctl(native, EPOLL_CTL_DEL, channel.socket.native, null)
        }
    }

    private val elements = HashMap<SOCKET, SelectorKeyImpl>()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {
        channel as NetworkChannel
        return memScoped {
            val event = alloc<epoll_event>()
            val key = SelectorKeyImpl(channel, attachment)
            event.events = (EPOLLIN or EPOLLRDHUP).convert()

            if (channel.socket.blocking)
                throw IllegalBlockingModeException()

            channel.regSelector(this@SocketSelector, key)
            event.data.sock = channel.socket.native
            epoll_ctl(native, EPOLL_CTL_ADD, channel.socket.native, event.ptr)

            elements[channel.socket.native] = key
            key
        }
    }

    actual fun process(timeout: Int?, func: (SelectorKey) -> Unit): Boolean {
        val count = epoll_wait(native, list, connections, timeout ?: -1)
        if (count <= 0)
            return false
        for (i in 0 until count) {
            val item = list[i]

            val el = elements[item.data.sock] ?: continue
            if (item.events.convert<Int>() and EPOLLRDHUP.convert() != 0) {
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
        actual fun cancel()
    }

}