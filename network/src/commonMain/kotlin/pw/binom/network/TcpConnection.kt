package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.AsyncChannel
import pw.binom.io.Channel
import pw.binom.io.IOException
import pw.binom.popOrNull
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class TcpConnection(val channel: TcpClientSocketChannel) : AbstractConnection(), AsyncChannel {

    var connect: Continuation<Unit>? = null

    lateinit var holder: CrossThreadKeyHolder

    private class ReadData {
        var continuation: Continuation<Int>? = null
        var data: ByteBuffer? = null
        var full = false

        fun reset() {
            continuation = null
            data = null
        }
    }

    private class SendData {
        var continuation: Continuation<Int>? = null
        var data: ByteBuffer? = null
        fun reset() {
            continuation = null
            data = null
        }
    }

    private val readData = ReadData()
    private val sendData = SendData()

    override fun readyForWrite(): Boolean {

        val waiter = holder.readyForWriteListener.popOrNull()
        if (waiter != null) {
            var exception: Throwable? = null
            try {
                waiter()
            } catch (e: Throwable) {
                exception = e
            }
            if (exception != null) {
                throw exception
            }
            return false
        }

        if (sendData.continuation == null) {
            return false
        }

        val result = runCatching { channel.write(sendData.data!!) }
        return if (sendData.data!!.remaining == 0) {
            sendData.continuation!!.resumeWith(result)
            sendData.reset()
            false
        } else {
            true
        }
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
        if (readData.continuation == null) {
            return false
        }
        val readed = runCatching { channel.read(readData.data!!) }
        return if (readData.full) {
            if (readData.data!!.remaining == 0) {
                readData.continuation!!.resumeWith(readed)
                readData.reset()
                false
            } else {
                true
            }
        } else {
            readData.continuation!!.resumeWith(readed)
            false
        }
    }


    override fun close() {
        TODO("Not yet implemented")
    }

    override suspend fun asyncClose() {
        TODO("Not yet implemented")
    }

    override suspend fun write(data: ByteBuffer): Int {
        val l = data.remaining
        if (l == 0)
            return 0

        if (sendData.continuation != null) {
            throw IllegalStateException("Connection already have write listener")
        } else {
            val wrote = channel.write(data)
            if (wrote == l) {
                return wrote
            }
        }
        suspendCoroutine<Int> {
            sendData.continuation = it
            holder.key.listensFlag = holder.key.listensFlag or Selector.EVENT_EPOLLOUT
        }
        return l
    }

    override suspend fun flush() {
        TODO("Not yet implemented")
    }

    override val available: Int
        get() = -1

    override suspend fun read(dest: ByteBuffer): Int {
        if (dest.remaining == 0) {
            return 0
        }
        if (readData.continuation != null) {
            throw IllegalStateException("Connection already have read listener")
        }
        readData.full = false
        return suspendCoroutine {
            readData.continuation = it
            readData.data = dest
            holder.key.listensFlag = holder.key.listensFlag or Selector.EVENT_EPOLLIN
        }
    }
}