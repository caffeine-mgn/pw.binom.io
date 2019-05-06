package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.linux.*
import platform.posix.close
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
        close(native)
        elements.clear()
    }

    private inner class SelectorKeyImpl(override val channel: Channel, override val attachment: Any?) : SelectorKey {
        override fun cancel() {
            val socket = when (channel) {
                is ServerSocketChannel -> channel.socket.socket.native
                is SocketChannel -> channel.socket.native
                else -> TODO()
            }
            elements.remove(socket)
            epoll_ctl(native, EPOLL_CTL_DEL, socket, null)
        }
    }

    private val elements = HashMap<Int, SelectorKeyImpl>()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {

        val socket = when (channel) {
            is ServerSocketChannel -> channel.socket.socket
            is SocketChannel -> channel.socket
            else -> TODO()
        }
        if (socket.blocking)
            throw IllegalBlockingModeException()

        memScoped {
            val event = alloc<epoll_event>()
            event.events = (EPOLLIN or EPOLLRDHUP).convert()
            event.data.fd = socket.native
            epoll_ctl(native, EPOLL_CTL_ADD, socket.native, event.ptr)
        }

        val e = SelectorKeyImpl(channel, attachment)
        elements[socket.native] = e
        e
    }

//    fun unreg(channel: Channel) {
//        val socket = when (channel) {
//            is ServerSocketChannel -> channel.socket.socket.native
//            is SocketChannel -> channel.socket.native
//            else -> TODO()
//        }
//        elements.remove(socket)
//        epoll_ctl(native, EPOLL_CTL_DEL, socket, null)
//    }

    actual fun process(timeout: Int?, func: (SelectorKey) -> Unit): Boolean {
        val count = epoll_wait(native, list, connections, timeout ?: -1)
        if (count <= 0)
            return false
        for (i in 0 until count) {
            val item = list[i]

            val el = elements[item.data.fd] ?: continue

            if (item.events.convert<Int>() and EPOLLRDHUP.convert() != 0) {
                when (el.channel){
                    is SocketChannel->el.channel.socket.internalDisconnected()
                    is ServerSocketChannel->el.channel.socket.socket.internalDisconnected()
                }
                el.cancel()
                elements.remove(item.data.fd)
            }
            func(el)
        }
        return true
    }

    actual interface SelectorKey {
        actual val channel: Channel
        actual val attachment: Any?
        actual fun cancel()
    }

    actual val keys: Collection<SelectorKey>
        get() = elements.values

}