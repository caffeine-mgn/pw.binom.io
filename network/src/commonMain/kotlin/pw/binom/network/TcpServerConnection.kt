package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.io.socket.*
import pw.binom.io.use
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TcpServerConnection constructor(
    val dispatcher: NetworkManager,
    val channel: TcpNetServerSocket,
    private var currentKey: SelectorKey
) : AbstractConnection() {
    var description: String? = null

    companion object {
        fun randomPort() = Socket.createTcpServerNetSocket().use {
            it.bind(NetworkAddress.create(host = "127.0.0.1", port = 0))
            it.port!!
        }
    }

    override fun toString(): String =
        if (description == null) {
            "TcpServerConnection"
        } else {
            "TcpServerConnection($description)"
        }

    override fun readyForWrite(key: SelectorKey) {
        // Do nothing
    }

    override suspend fun connection() {
        error()
    }

    val port
        get() = channel.port!!

    override fun error() {
        // ignore error
//        close()
    }

    override fun readyForRead(key: SelectorKey) {
        val acceptListener = acceptListener ?: return
        val newChannel = channel.accept(null) ?: return
        this.acceptListener = null
        acceptListener.resume(newChannel)
    }

    override fun close() {
        acceptListener?.also {
            if (it.isActive) {
                it.resumeWithException(SocketClosedException())
            }
            acceptListener = null
        }
        if (!currentKey.isClosed) {
            currentKey.close()
        }
        channel.close()
    }

    private var acceptListener: CancellableContinuation<TcpClientNetSocket>? = null

    suspend fun accept(address: MutableNetworkAddress? = null): TcpConnection {
        check(acceptListener == null) { "Connection already have read listener" }

        val newClient = channel.accept(address)
        if (newClient != null) {
            return dispatcher.attach(newClient).also {
                it.description = "Server of $description"
            }
        }

        val newChannel = suspendCancellableCoroutine<TcpClientSocket> { con ->
            acceptListener = con
            currentKey.listenFlags = KeyListenFlags.READ or KeyListenFlags.ERROR
            currentKey.selector.wakeup()
            con.invokeOnCancellation {
                acceptListener = null
                currentKey.listenFlags = 0
                currentKey.selector.wakeup()
            }
        }
        return dispatcher.attach(newChannel).also {
            it.description = "Server of $description"
        }
    }
}
