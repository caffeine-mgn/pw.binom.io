package pw.binom.network

import pw.binom.atomic.AtomicInt
import pw.binom.io.Closeable

actual class Selector2 : Closeable {

    val pool = PoolSelector()

    actual fun attach(socket: TcpClientSocketChannel, mode: Int, attachment: Any?) {
        val key = pool.attach(socket.native, epollCommonToNative(mode), attachment)
    }

    actual fun attach(socket: TcpServerSocketChannel, mode: Int, attachment: Any?) {
        val key = pool.attach(socket.native, epollCommonToNative(mode), attachment)
    }

    actual fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?) {
        val key = pool.attach(socket.native, epollCommonToNative(mode), attachment)
    }

    actual fun wait(timeout: Long, func: (Any?, mode: Int) -> Unit): Boolean =
        pool.wait(timeout) { attachment, mode ->
            func(attachment.attachment, epollNativeToCommon(mode))
        }

    override fun close() {
        pool.close()
    }

    actual companion object {
        actual val EVENT_EPOLLIN: Int = 0b0001
        actual val EVENT_EPOLLOUT: Int = 0b0010
        actual val EVENT_CONNECTED: Int = 0b1000
        actual val EVENT_ERROR: Int = 0b10000
    }

    actual class Key(val nativeKey: PoolSelector.NativeSelectorKey) : Closeable {
        actual var mode: Int
            get() = epollNativeToCommon(nativeKey.mode)
            set(value) {
                nativeKey.mode = epollCommonToNative(value)
            }

        override fun close() {
            nativeKey.close()
        }

        actual val attachment: Any?
            get() = nativeKey.attachment?.value
    }
}