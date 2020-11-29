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
        val newChannel = channel.accept(null) ?: return true
        val acceptListener = acceptListener ?: return false
        acceptListener.resume(newChannel)
        return false
    }

    override fun close() {
        channel.close()
    }

    private var acceptListener: Continuation<TcpClientSocketChannel>? = null

    suspend fun accept(address: NetworkAddress.Mutable? = null): TcpConnection? {
        val newChannel = suspendCoroutine<TcpClientSocketChannel> { con ->
            acceptListener = con
            key.listensFlag = Selector.INPUT_READY
        }
//        val newChannel = channel.accept(null) ?: return null
        return dispatcher.attach(newChannel)
    }
}