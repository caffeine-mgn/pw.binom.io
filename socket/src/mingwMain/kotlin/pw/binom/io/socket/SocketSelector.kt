package pw.binom.io.socket

import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.malloc
import pw.binom.io.Closeable
import pw.binom.io.cinterop.wepoll.*
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
    }

    private inner class SelectorKeyImpl(override val channel: Channel, override val attachment: Any?) : SelectorKey {
        override fun cancel() {
            val socket = when (channel) {
                is ServerSocketChannel -> channel.socket.socket
                is SocketChannel -> channel.socket
                else -> TODO()
            }
            elements.remove(socket.native)
            epoll_ctl(native, EPOLL_CTL_DEL, socket.native, null)
        }
    }

    private val elements = HashMap<SOCKET, SelectorKeyImpl>()

    actual fun reg(channel: Channel, attachment: Any?): SelectorKey {
        return memScoped {
            val event = alloc<epoll_event>()
            event.events = (EPOLLIN).convert();// | EPOLLET;
            val socket = when (channel) {
                is ServerSocketChannel -> channel.socket.socket
                is SocketChannel -> channel.socket
                else -> TODO()
            }
            if (socket.blocking)
                throw IllegalBlockingModeException()
            event.data.sock = socket.native
            epoll_ctl(native, EPOLL_CTL_ADD, socket.native, event.ptr)
            val key = SelectorKeyImpl(channel, attachment)
            elements[socket.native] = key
            key
        }
    }

    actual fun process(func: (SelectorKey) -> Unit): Boolean {
        val count = epoll_wait(native, list, connections, -1)
        if (count <= 0)
            return false
        for (i in 0 until count) {
            val el = elements[list[i].data.sock] ?: continue
//            val EPOLLIN = list[i].events and EPOLLIN > 0u
//            val EPOLLOUT = list[i].events and EPOLLOUT > 0u
//            val EPOLLRDHUP = list[i].events and EPOLLRDHUP > 0u
//            println("EPOLLIN=$EPOLLIN EPOLLOUT=$EPOLLOUT EPOLLRDHUP=$EPOLLRDHUP")

            func(el)
        }
        return true
    }

    actual interface SelectorKey {
        actual val channel: Channel
        actual val attachment: Any?
        actual fun cancel()
    }

}