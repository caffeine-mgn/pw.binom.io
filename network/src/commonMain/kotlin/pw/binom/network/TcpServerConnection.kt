package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import pw.binom.CancelledException
import pw.binom.io.use
import kotlin.coroutines.*

class TcpServerConnection internal constructor(val dispatcher: NetworkCoroutineDispatcher, val channel: TcpServerSocketChannel) :
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
            key.removeListen(Selector.INPUT_READY)
            return
        }
        val newChannel = channel.accept(null)
        if (newChannel == null) {
            if (acceptListener == null) {
                key.removeListen(Selector.INPUT_READY)
            }
            return
        }
        val acceptListener = acceptListener
        if (acceptListener == null) {
            key.removeListen(Selector.INPUT_READY)
            throw IllegalStateException("Socket accepted, but listener is null")
        }
        this.acceptListener = null
        acceptListener.resume(newChannel)
        if (this.acceptListener == null) {
            key.removeListen(Selector.INPUT_READY)
        }
        return
    }

    override fun close() {
        acceptListener?.resumeWithException(SocketClosedException())
        acceptListener = null
        channel.close()
    }

    private var acceptListener: CancellableContinuation<TcpClientSocketChannel>? = null

    fun interruptAccepting(): Boolean {
        val continuation = acceptListener ?: return false
        continuation.resumeWithException(CancelledException())
        acceptListener = null
        return true
    }

    suspend fun accept(address: NetworkAddress.Mutable? = null): TcpConnection =
        withContext(dispatcher) TT@{
            if (acceptListener != null) {
                throw IllegalStateException("Connection already have read listener")
            }

            val newClient = channel.accept(address)
            if (newClient != null) {
                return@TT dispatcher.attach(newClient)
            }
            val newChannel = suspendCancellableCoroutine<TcpClientSocketChannel> { con ->
                acceptListener = con
                key.listensFlag = Selector.INPUT_READY
                con.invokeOnCancellation {
                    acceptListener = null
                    key.listensFlag = 0
                }
            }
            return@TT dispatcher.attach(newChannel)
        }

    override fun cancelSelector() {
        acceptListener?.cancel()
        acceptListener = null
    }
}