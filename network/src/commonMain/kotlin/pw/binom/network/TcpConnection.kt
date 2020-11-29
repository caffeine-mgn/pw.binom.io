package pw.binom.network

import pw.binom.io.IOException
import kotlin.coroutines.Continuation

class TcpConnection(val channel: TcpClientSocketChannel) : AbstractConnection() {

    var connect: Continuation<Unit>? = null

    override fun readyForWrite(): Boolean {
        return false
    }

    override fun connected() {
        println("TcpConnection->connected")
        connect?.resumeWith(Result.success(Unit))
    }

    override fun error() {
        println("TcpConnection->error")
        connect?.resumeWith(Result.failure(SocketConnectException()))
    }

    override fun readyForRead(): Boolean {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}