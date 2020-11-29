package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.AsyncChannel
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
                holder.key.removeListen(Selector.OUTPUT_READY)
                throw exception
            }
            holder.key.removeListen(Selector.OUTPUT_READY)
            return false
        }

        if (sendData.continuation == null) {
            holder.key.removeListen(Selector.OUTPUT_READY)
            return false
        }

        val result = runCatching { channel.write(sendData.data!!) }
        return if (sendData.data!!.remaining == 0) {
            sendData.continuation!!.resumeWith(result)
            sendData.reset()
            holder.key.removeListen(Selector.OUTPUT_READY)
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
            println("Not need to read...")
            holder.key.removeListen(Selector.INPUT_READY)
            return false
        }
        println("Reading...")
        val readed = runCatching { channel.read(readData.data!!) }
        println("readed $readed")
        return if (readData.full) {
            if (readData.data!!.remaining == 0) {
                val con = readData.continuation!!
                readData.reset()
                holder.key.removeListen(Selector.INPUT_READY)
                con.resumeWith(readed)
                false
            } else {
                true
            }
        } else {
            val con = readData.continuation!!
            readData.reset()
            holder.key.removeListen(Selector.INPUT_READY)
            con.resumeWith(readed)
            false
        }
    }


    override fun close() {
        holder.key.listensFlag = 0
        channel.close()
        holder.key.close()
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
            holder.key.addListen(Selector.OUTPUT_READY)
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
        val readed = suspendCoroutine<Int> {
            readData.continuation = it
            readData.data = dest
            holder.key.addListen(Selector.INPUT_READY)
        }
        if (readed <= 0) {
            println("Socket Closed! readed: $readed")
            throw SocketClosedException()
        }
        return readed
    }
}