package pw.binom.io.socket

import kotlin.native.concurrent.ensureNeverFrozen

actual class ServerSocketChannel actual constructor() : NetworkChannel {
    var blocking: Boolean
        get() = socket.blocking
        set(value) {
            socket.blocking = value
        }
    private val keys = HashMap<SocketSelector, SocketSelector.SelectorKey>()

    override fun regSelector(selector: SocketSelector, key: SocketSelector.SelectorKey) {
        if (keys.containsKey(selector))
            throw IllegalArgumentException("Already is registered in selector")
        keys[selector] = key
    }

    override fun unregSelector(selector: SocketSelector) {
        val g = keys[selector] ?: throw IllegalArgumentException("Not registered in selector")
        g.cancel()
    }

    init {
        this.ensureNeverFrozen()
    }


    override fun close() {
        socket.close()
    }

    val server = SocketServer()
    override val socket: Socket
        get() = server.socket

    actual fun bind(port: Int) {
        socket.bind(port)
    }

    actual fun accept(): SocketChannel? {
        val socket = server.accept() ?: return null
        return SocketChannel(socket)
    }
}