package pw.binom.network

import pw.binom.io.Closeable

interface Selector : Closeable {

    interface KeyEvent {
        val key: Key
        val mode: Int

        val isInputReady
            get() = mode and Selector.INPUT_READY != 0
        val isOutputReady
            get() = mode and Selector.OUTPUT_READY != 0
        val isConnected
            get() = mode and Selector.EVENT_CONNECTED != 0
        val isError
            get() = mode and Selector.EVENT_ERROR != 0
    }

    interface Key : Closeable {
        val attachment: Any?
        var listensFlag: Int
        val closed: Boolean

        fun addListen(code: Int) {
            listensFlag = listensFlag or code
        }

        fun removeListen(code: Int) {
            listensFlag = (listensFlag.inv() or code).inv()
        }
    }

    companion object {
        fun open() = createSelector()
        const val INPUT_READY: Int = 0b0001
        const val OUTPUT_READY: Int = 0b0010
        const val EVENT_CONNECTED: Int = 0b0100
        const val EVENT_ERROR: Int = 0b1000
    }

    fun getAttachedKeys(): Collection<Key>

    /**
     * @param timeout Timeout for wait events. -1 infinity
     */
    fun select(timeout: Long = -1, func: (Key, mode: Int) -> Unit): Int
    fun select(timeout: Long = -1): Iterator<KeyEvent>
    fun attach(socket: TcpClientSocketChannel, mode: Int = 0, attachment: Any? = null): Key
    fun attach(socket: TcpServerSocketChannel, mode: Int = 0, attachment: Any? = null): Key
    fun attach(socket: UdpSocketChannel, mode: Int, attachment: Any?): Key

}

internal expect fun createSelector(): Selector
internal fun selectorModeToString(mode: Int): String {
    val sb = StringBuilder()
    if (mode and Selector.INPUT_READY != 0) {
        if (sb.isNotEmpty()) {
            sb.append(", ")
        }
        sb.append("INPUT_READY")
    }
    if (mode and Selector.OUTPUT_READY != 0) {
        if (sb.isNotEmpty()) {
            sb.append(", ")
        }
        sb.append("OUTPUT_READY")
    }
    if (mode and Selector.EVENT_CONNECTED != 0) {
        if (sb.isNotEmpty()) {
            sb.append(", ")
        }
        sb.append("EVENT_CONNECTED")
    }
    if (mode and Selector.EVENT_ERROR != 0) {
        if (sb.isNotEmpty()) {
            sb.append(", ")
        }
        sb.append("EVENT_ERROR")
    }
    return sb.toString()
}