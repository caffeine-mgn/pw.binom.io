package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import pw.binom.io.use
import kotlin.coroutines.*

class TcpServerConnection internal constructor(
    val dispatcher: NetworkCoroutineDispatcher,
    val channel: TcpServerSocketChannel
) :
    AbstractConnection() {

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
        if (acceptListener == null) {
            if (!key.closed) {
                key.removeListen(Selector.INPUT_READY)
            }
            return
        }
        val newChannel = channel.accept(null)
        if (newChannel == null) {
            if (acceptListener == null) {
                if (!key.closed) {
                    key.removeListen(Selector.INPUT_READY)
                }
            }
            return
        }
        val acceptListener = acceptListener
        if (acceptListener == null) {
            if (!key.closed) {
                key.removeListen(Selector.INPUT_READY)
            }
            throw IllegalStateException("Socket accepted, but listener is null")
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
            println("Start accepting")
            if (acceptListener != null) {
                println("!!!!")
                throw IllegalStateException("Connection already have read listener")
            }

            val newClient = channel.accept(address)
            if (newClient != null) {
                println("2222")
                return@TT dispatcher.attach(newClient)
            }
            println("Suspend...")
            val newChannel = suspendCancellableCoroutine<TcpClientSocketChannel> { con ->
                acceptListener = con
                key.listensFlag = Selector.INPUT_READY
                con.invokeOnCancellation {
                    println("Cancel accept called")
                    acceptListener = null
                    if (!key.closed) {
                        key.listensFlag = 0
                    }
                }
                println("on cancel waiter sent!")
            }
            return@TT dispatcher.attach(newChannel)
        }

    override fun cancelSelector() {
        acceptListener?.cancel()
        acceptListener = null
    }
}
