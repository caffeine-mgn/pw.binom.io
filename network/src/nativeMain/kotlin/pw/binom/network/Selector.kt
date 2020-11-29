package pw.binom.network

import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef
import pw.binom.io.Closeable

actual class Selector : Closeable {

    val pool = PoolSelector()

    actual fun attach(socket: TcpClientSocketChannel, mode: Int, attachment: Any?) {
        pool.attach(socket.native, mode, attachment?.let { StableRef.create(it) }?.asCPointer())
    }

    actual fun attach(socket: TcpServerSocketChannel, mode: Int, attachment: Any?) {
        pool.attach(socket.native, mode, attachment?.let { StableRef.create(it) }?.asCPointer())
    }

    actual fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?) {
        pool.attach(socket.native, mode, attachment?.let { StableRef.create(it) }?.asCPointer())
    }

    actual fun wait(timeout: Long, func: (Any?, mode: Int) -> Unit): Boolean =
        pool.wait(timeout) { attachment, mode ->
            func(attachment?.let { it.asStableRef<Any>() }, mode)
        }

    override fun close() {
        pool.close()
    }

    actual fun mode(socket: UdpSocketChannel, mode: Int, attachment: Any?) {
        pool.edit(socket.native, mode, attachment?.let { StableRef.create(it) }?.asCPointer())
    }

    actual fun mode(socket: TcpClientSocketChannel, mode: Int, attachment: Any?) {
        pool.edit(socket.native, mode, attachment?.let { StableRef.create(it) }?.asCPointer())
    }

    actual fun mode(socket: TcpServerSocketChannel, mode: Int, attachment: Any?) {
        pool.edit(socket.native, mode, attachment?.let { StableRef.create(it) }?.asCPointer())
    }

    actual companion object {
        actual val EVENT_EPOLLIN: Int = 0b0001
        actual val EVENT_EPOLLOUT: Int = 0b0010
        actual val EVENT_EPOLLRDHUP: Int = 0b0100
        actual val EVENT_CONNECTED: Int = 0b1000
        actual val EVENT_ERROR: Int = 0b10000
    }
}