package pw.binom.network

import pw.binom.io.Closeable

expect class Selector2() : Closeable {
    companion object {
        val EVENT_EPOLLIN: Int
        val EVENT_EPOLLOUT: Int

        /**
         *
         */
        val EVENT_CONNECTED: Int
        val EVENT_ERROR: Int
    }

    fun attach(socket: TcpClientSocketChannel, mode: Int = 0, attachment: Any? = null)
    fun attach(socket: TcpServerSocketChannel, mode: Int = 0, attachment: Any? = null)
    fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?)
    fun wait(timeout: Long = -1, func: (Any?, mode: Int) -> Unit): Boolean

    class Key : Closeable {
        var mode: Int
        val attachment: Any?
    }
}