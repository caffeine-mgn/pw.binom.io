package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.io.ClosedException
import pw.binom.io.socket.*
import pw.binom.io.use
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TcpServerConnection constructor(
    val dispatcher: NetworkManager,
    val channel: TcpNetServerSocket,
    private var currentKey: SelectorKey,
) : AbstractConnection() {
    var description: String? = null

    var state: ConnectionState = ConnectionState.IDLE
        private set

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

    private var closed = false

    override fun error() {
        channel.closeAnyway()
        currentKey.closeAnyway()
        val acceptListener = acceptListener
        if (acceptListener != null) {
            this.acceptListener = null
            acceptListener.resumeWithException(SocketClosedException())
            state = ConnectionState.IDLE
        }
        // ignore error
//        close()
    }

    override fun readyForRead(key: SelectorKey) {
//        println("TcpServerConnection::readyForRead")
        val acceptListener = acceptListener
        if (acceptListener == null) {
//            println("TcpServerConnection::readyForRead acceptListener == null")
            return
        }
        state = ConnectionState.PROCESS
        val newChannel = channel.accept(null)
        if (newChannel == null) {
//            println("TcpServerConnection::readyForRead newChannel == null")
            ConnectionState.SUSPENDED
            return
        }
        this.acceptListener = null
//        println("TcpServerConnection::readyForRead resume...")
        state = ConnectionState.IDLE
        acceptListener.resume(newChannel)
    }

    override fun close() {
        acceptListener?.also {
            if (it.isActive) {
                it.resumeWithException(SocketClosedException())
            }
            acceptListener = null
        }
        state = ConnectionState.CLOSED
        if (!currentKey.isClosed) {
            currentKey.close()
        }
        channel.close()
    }

    private var acceptListener: CancellableContinuation<TcpClientNetSocket>? = null

    suspend fun accept(address: MutableNetworkAddress? = null): TcpConnection {
        if (closed) {
//            println("TcpServerConnection::accept closed==true")
            throw SocketClosedException()
        }
        check(acceptListener == null) { "Connection already have read listener" }

        state = ConnectionState.PROCESS
        val newClient = channel.accept(address)
        if (newClient != null) {
//            println("TcpServerConnection::accept accepted!!!")
            state = ConnectionState.IDLE
            return dispatcher.attach(newClient).also {
                it.description = "Server of $description"
            }
        }
        val newChannel = suspendCancellableCoroutine<TcpClientSocket> { con ->
            try {
//                println("TcpServerConnection::accept newClient==null. Wait event...")
                state = ConnectionState.SUSPENDED
                acceptListener = con
                if (currentKey.updateListenFlags(KeyListenFlags.READ or KeyListenFlags.ONCE)) {
                    currentKey.selector.wakeup()
                } else {
                    try {
                        close()
                        con.resumeWithException(ClosedException())
                    } catch (e: Throwable) {
                        val ex = ClosedException()
                        ex.addSuppressed(e)
                        con.resumeWithException(ex)
                    }
                    return@suspendCancellableCoroutine
                }
            } catch (e: Throwable) {
                con.resumeWithException(e)
            }
            con.invokeOnCancellation {
//                println("TcpServerConnection::accept canceled waiting")
                acceptListener = null
                if (currentKey.updateListenFlags(0)) {
                    currentKey.selector.wakeup()
                    state = ConnectionState.IDLE
                } else {
                    close()
                }
            }
        }
        return dispatcher.attach(newChannel).also {
            it.description = "Server of $description"
        }
    }
}
