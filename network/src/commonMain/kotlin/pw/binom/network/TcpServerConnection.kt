package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.io.use
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TcpServerConnection constructor(
    val dispatcher: NetworkManager,
    val channel: TcpServerSocketChannel
) : AbstractConnection() {
    var description: String? = null

    companion object {
        fun randomPort() = TcpServerSocketChannel().use {
            it.bind(NetworkAddress.Immutable(host = "127.0.0.1", port = 0))
            it.port!!
        }
    }

    override fun toString(): String =
        if (description == null) {
            "TcpServerConnection"
        } else {
            "TcpServerConnection($description)"
        }

    val keys = KeyCollection()

    override fun readyForWrite(key: Selector.Key) {
        // Do nothing
    }

    val port
        get() = channel.port!!

    override fun connecting() {
        throw RuntimeException("Not supported")
    }

    override fun connected() {
        throw RuntimeException("Not supported")
    }

    override fun error() {
        throw RuntimeException("Not supported")
    }

    override fun readyForRead(key: Selector.Key) {
        val acceptListener = acceptListener
        if (acceptListener == null) {
            keys.removeListen(Selector.INPUT_READY)
            return
        }
        val newChannel = channel.accept(null)
        if (newChannel == null) {
            keys.removeListen(Selector.INPUT_READY)
            return
        }

        this.acceptListener = null
        acceptListener.resume(newChannel)
        if (this.acceptListener == null) {
            keys.removeListen(Selector.INPUT_READY)
        }
        return
    }

    override fun close() {
        acceptListener?.also {
            if (it.isActive) {
                it.resumeWithException(SocketClosedException())
            }
            acceptListener = null
        }
        keys.close()
        channel.close()
    }

    private var acceptListener: CancellableContinuation<TcpClientSocketChannel>? = null

    suspend fun accept(address: NetworkAddress.Mutable? = null): TcpConnection {
        keys.checkEmpty()

        if (acceptListener != null) {
            throw IllegalStateException("Connection already have read listener")
        }

        val newClient = channel.accept(address)
        if (newClient != null) {
            val c = dispatcher.attach(newClient)
//            var c: TcpConnection? = null
//            dispatcher.let { dispatcher ->
//                if (c == null) {
//                    c = dispatcher.attach(newClient)
//                } else {
//                    dispatcher.attach(c!!)
//                }
//            }
            c.description = "Server of $description"
            return c
        }

        val newChannel = suspendCancellableCoroutine<TcpClientSocketChannel> { con ->
            acceptListener = con
            keys.setListensFlag(Selector.INPUT_READY)
            keys.wakeup()
            con.invokeOnCancellation {
                acceptListener = null
                keys.setListensFlag(0)
            }
        }

        val c = dispatcher.attach(newChannel)
        c.description = "Server of $description"
        return c
    }

    override fun cancelSelector() {
        acceptListener?.cancel()
        acceptListener = null
    }
}
