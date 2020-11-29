package pw.binom.network

import pw.binom.io.Closeable

interface Selector:Closeable {

    interface Key : Closeable {
        val attachment: Any?
        var listensFlag: Int
        val eventsFlag: Int
    }

    companion object {
        fun open() = createSelector()
        const val EVENT_EPOLLIN: Int = 0b0001
        const val EVENT_EPOLLOUT: Int = 0b0010
        const val EVENT_CONNECTED: Int = 0b0100
        const val EVENT_ERROR: Int = 0b1000
    }

    /**
     * @param timeout Timeout for wait events. -1 infinity
     */
    fun select(timeout: Long = -1, func: (Key, mode: Int) -> Unit): Int
    fun attach(socket: TcpClientSocketChannel, mode: Int = 0, attachment: Any? = null): Key
    fun attach(socket: TcpServerSocketChannel, mode: Int = 0, attachment: Any? = null): Key
    fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?): Key

}

internal expect fun createSelector(): Selector