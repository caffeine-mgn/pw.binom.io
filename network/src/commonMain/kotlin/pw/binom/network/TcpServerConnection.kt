package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import pw.binom.io.use
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TcpServerConnection constructor(
    val dispatcher: NetworkManager,
    val channel: TcpServerSocketChannel
) : AbstractConnection() {
    var description: String = "TcpServer"

    companion object {
        fun randomPort() = TcpServerSocketChannel().use {
            it.bind(NetworkAddress.Immutable(host = "127.0.0.1", port = 0))
            it.port!!
        }
    }

    lateinit var key: Selector.Key

    override fun readyForWrite() {
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
            if (!key.closed) {
                key.removeListen(Selector.INPUT_READY)
            }
            return
        }
        val newChannel = channel.accept(null)
        if (newChannel == null) {
            if (!key.closed) {
                key.removeListen(Selector.INPUT_READY)
            }
            return
        }

        this.acceptListener = null
        acceptListener.resume(newChannel)
        if (this.acceptListener == null) {
            if (!key.closed) {
                key.removeListen(Selector.INPUT_READY)
            }
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
        channel.close()
    }

    private var acceptListener: CancellableContinuation<TcpClientSocketChannel>? = null

    suspend fun accept(address: NetworkAddress.Mutable? = null): TcpConnection =
        withContext(dispatcher) TT@{
            if (acceptListener != null) {
                throw IllegalStateException("Connection already have read listener")
            }

            val newClient = channel.accept(address)
            if (newClient != null) {
                val c = dispatcher.attach(newClient)
                c.description = "Client of $description"
                return@TT c
            }
            val newChannel = suspendCancellableCoroutine<TcpClientSocketChannel> { con ->
                acceptListener = con
                key.listensFlag = Selector.INPUT_READY
                con.invokeOnCancellation {
                    acceptListener = null
                    if (!key.closed) {
                        key.listensFlag = 0
                    }
                }
            }
            val c = dispatcher.attach(newChannel)
            c.description = "Client of $description"
            return@TT c
        }

    override fun cancelSelector() {
        acceptListener?.cancel()
        acceptListener = null
    }
}
