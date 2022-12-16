package pw.binom.network

import pw.binom.io.Closeable

interface SelectorOld : Closeable {

    interface KeyEvent {
        val key: Key
        val mode: Int

        val isInputReady
            get() = mode and INPUT_READY != 0
        val isOutputReady
            get() = mode and OUTPUT_READY != 0
        val isConnected
            get() = mode and EVENT_CONNECTED != 0
        val isError
            get() = mode and EVENT_ERROR != 0
    }

    interface Key : Closeable {
        val attachment: Any?
        var listensFlag: Int
        val closed: Boolean
        val selector: SelectorOld

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

    fun wakeup()

    fun getAttachedKeys(): Collection<Key>

    /**
     * @param timeout Timeout for wait events. -1 infinity
     */
    fun select(selectedEvents: SelectedEventsOld, timeout: Long = -1): Int
    fun attach(socket: TcpClientSocketChannel, attachment: Any? = null, mode: Int = 0): Key
    fun attach(socket: TcpServerSocketChannel, attachment: Any? = null, mode: Int = 0): Key
    fun attach(socket: UdpSocketChannel, attachment: Any? = null, mode: Int = 0): Key
}

internal expect fun createSelector(): SelectorOld
internal fun selectorModeToString(mode: Int): String {
    val sb = StringBuilder()
    if (mode and SelectorOld.INPUT_READY != 0) {
        if (sb.isNotEmpty()) {
            sb.append(", ")
        }
        sb.append("INPUT_READY")
    }
    if (mode and SelectorOld.OUTPUT_READY != 0) {
        if (sb.isNotEmpty()) {
            sb.append(", ")
        }
        sb.append("OUTPUT_READY")
    }
    if (mode and SelectorOld.EVENT_CONNECTED != 0) {
        if (sb.isNotEmpty()) {
            sb.append(", ")
        }
        sb.append("EVENT_CONNECTED")
    }
    if (mode and SelectorOld.EVENT_ERROR != 0) {
        if (sb.isNotEmpty()) {
            sb.append(", ")
        }
        sb.append("EVENT_ERROR")
    }
    return sb.toString().trim()
}
