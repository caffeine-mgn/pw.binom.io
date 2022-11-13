package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.collections.defaultMutableSet
import pw.binom.io.use
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TcpServerConnection constructor(
//    val dispatcher: NetworkManager,
    val channel: TcpServerSocketChannel
) : AbstractConnection() {
    var description: String? = null
    internal var dispatchers = defaultMutableSet<NetworkManager>()

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

    internal var key = KeyCollection()

    override fun readyForWrite() {
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

    override fun readyForRead() {
        val acceptListener = acceptListener
        if (acceptListener == null) {
            key.removeListen(Selector.INPUT_READY)
            return
        }
        val newChannel = channel.accept(null)
        if (newChannel == null) {
            key.removeListen(Selector.INPUT_READY)
            return
        }

        this.acceptListener = null
        acceptListener.resume(newChannel)
        if (this.acceptListener == null) {
            key.removeListen(Selector.INPUT_READY)
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
        key.close()
        channel.close()
        dispatchers.clear()
    }

    private var acceptListener: CancellableContinuation<TcpClientSocketChannel>? = null

    suspend fun accept(address: NetworkAddress.Mutable? = null): TcpConnection {
        key.checkEmpty()

        if (acceptListener != null) {
            throw IllegalStateException("Connection already have read listener")
        }

        val newClient = channel.accept(address)
        if (newClient != null) {
            var c: TcpConnection? = null
            dispatchers.forEach { dispatcher ->
                if (c == null) {
                    c = dispatcher.attach(newClient)
                } else {
                    dispatcher.attach(c!!)
                }
            }
            c!!.description = "Server of $description"
            return c!!
        }

        val newChannel = suspendCancellableCoroutine<TcpClientSocketChannel> { con ->
            acceptListener = con
            key.setListensFlag(Selector.INPUT_READY)
            key.wakeup()
            con.invokeOnCancellation {
                acceptListener = null
                key.setListensFlag(0)
            }
        }

        var c: TcpConnection? = null
        dispatchers.forEach { dispatcher ->
            if (c == null) {
                c = dispatcher.attach(newChannel)
            } else {
                dispatcher.attach(c!!)
            }
        }
        c!!.description = "Server of $description"
        return c!!
    }

    override fun cancelSelector() {
        acceptListener?.cancel()
        acceptListener = null
    }
}
