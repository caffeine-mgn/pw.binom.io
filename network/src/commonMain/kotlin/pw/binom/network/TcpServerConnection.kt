package pw.binom.network

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TcpServerConnection(val dispatcher: NetworkDispatcher, val channel: TcpServerSocketChannel) :
    AbstractConnection() {

    lateinit var key: Selector.Key

    override fun readyForWrite() {
        TODO("Not yet implemented")
    }

    override fun connected() {
        TODO("Not yet implemented")
    }

    override fun error() {
        TODO("Not yet implemented")
    }

    override fun readyForRead() {
        if (acceptListener == null) {
            key.listensFlag = 0
            return
        }
        val newChannel = channel.accept(null)
        if (newChannel == null) {
            if (acceptListener == null) {
                key.listensFlag = 0
            }
            return
        }
        val acceptListener = acceptListener
        if (acceptListener == null) {
            key.listensFlag = 0
            throw IllegalStateException("Socket accepted, but listener is null")
        }
        this.acceptListener = null
        acceptListener.resume(newChannel)
        if (this.acceptListener == null) {
            key.listensFlag = 0
        }
        return
    }

    override fun close() {
        acceptListener?.resumeWithException(SocketClosedException())
        acceptListener = null
        channel.close()
    }

    private var acceptListener: Continuation<TcpClientSocketChannel>? = null

    suspend fun accept(address: NetworkAddress.Mutable? = null): TcpConnection? {
        if (acceptListener != null) {
            throw IllegalStateException("Already Accepting")
        }
        val newChannel = suspendCoroutine<TcpClientSocketChannel> { con ->
            acceptListener = con
            key.listensFlag = Selector.INPUT_READY
        }
        return dispatcher.attach(newChannel)
    }
}