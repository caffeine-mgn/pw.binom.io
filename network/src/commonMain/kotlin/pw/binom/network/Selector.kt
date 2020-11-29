package pw.binom.network

import pw.binom.io.Closeable

expect class Selector() : Closeable {
    companion object {
        val EVENT_EPOLLIN: Int
        val EVENT_EPOLLOUT: Int
        val EVENT_EPOLLRDHUP: Int

        /**
         *
         */
        val EVENT_CONNECTED: Int
        val EVENT_ERROR: Int
    }

    fun attach(socket: TcpClientSocketChannel, mode: Int = 0, attachment: Any? = null)
    fun attach(socket: TcpServerSocketChannel, mode: Int = 0, attachment: Any? = null)
    fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?)
    fun mode(socket: TcpClientSocketChannel, mode: Int, attachment: Any?)
    fun mode(socket: TcpServerSocketChannel, mode: Int, attachment: Any?)
    fun mode(socket: UdpSocketChannel, mode: Int, attachment: Any?)
    fun wait(timeout: Long = -1, func: (Any?, mode: Int) -> Unit): Boolean
}