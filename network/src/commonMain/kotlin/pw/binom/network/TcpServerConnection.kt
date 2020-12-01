package pw.binom.network

import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TcpServerConnection(val dispatcher: NetworkDispatcher, val channel: TcpServerSocketChannel) :
    AbstractConnection() {

    lateinit var key: Selector.Key

    override fun readyForWrite(): Boolean {
        TODO("Not yet implemented")
    }

    override fun connected() {
        TODO("Not yet implemented")
    }

    override fun error() {
        TODO("Not yet implemented")
    }

    override fun readyForRead(): Boolean {
        if (acceptListener == null) {
            key.listensFlag = 0
            return false
        }
        val newChannel = channel.accept(null)
        if (newChannel == null) {
            if (acceptListener == null) {
                key.listensFlag = 0
            }
            return false
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
        return false
    }

    override fun close() {
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