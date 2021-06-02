package pw.binom.network

import pw.binom.CancelledException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TcpServerConnection(val dispatcher: NetworkDispatcher, val channel: TcpServerSocketChannel) :
    AbstractConnection() {

    lateinit var key: Selector.Key

    override fun readyForWrite() {

    }

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

    private var acceptListener: Continuation<TcpClientSocketChannel>? = null

    fun interruptAccepting(): Boolean {
        val continuation = acceptListener ?: return false
        continuation.resumeWithException(CancelledException())
        acceptListener = null
        return true
    }

    suspend fun accept(address: NetworkAddress.Mutable? = null): TcpConnection? {
        if (acceptListener != null) {
            throw IllegalStateException("Connection already have read listener")
        }

        val newClient = channel.accept(address)
        if (newClient != null) {
            return dispatcher.attach(newClient)
        }

        val newChannel = suspendCoroutine<TcpClientSocketChannel> { con ->
            acceptListener = con
            key.listensFlag = Selector.INPUT_READY
        }
        return dispatcher.attach(newChannel)
    }
}