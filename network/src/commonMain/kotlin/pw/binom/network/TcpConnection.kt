package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.io.AsyncChannel
import pw.binom.popOrNull
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
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

    private fun calcListenFlags() =
        when {
            readData.continuation != null && (!holder.readyForWriteListener.isEmpty || sendData.continuation != null) -> Selector.INPUT_READY or Selector.OUTPUT_READY
            readData.continuation != null -> Selector.INPUT_READY
            !holder.readyForWriteListener.isEmpty || sendData.continuation != null -> Selector.OUTPUT_READY
            else -> 0
        }

    override fun readyForWrite() {
        if (sendData.continuation != null) {
            val result = runCatching { channel.write(sendData.data!!) }
            if (sendData.data!!.remaining == 0) {
                val con = sendData.continuation!!
                sendData.reset()
                holder.key.removeListen(Selector.OUTPUT_READY)
                con.resumeWith(result)
            }
            holder.key.listensFlag = calcListenFlags()
            return
        }

        val waiter = holder.readyForWriteListener.popOrNull()
        if (waiter != null) {
            var exception: Throwable? = null
            try {
                waiter()
            } catch (e: Throwable) {
                exception = e
            }
            if (exception != null) {
                if (!holder.key.closed) {
                    holder.key.removeListen(Selector.OUTPUT_READY)
                }
                throw exception
            }
            if (sendData.continuation == null && !holder.key.closed) {
                holder.key.removeListen(Selector.OUTPUT_READY)
            }
        }
        holder.key.listensFlag = calcListenFlags()
    }

    override fun connected() {
        val connect = connect
        this.connect = null
        connect?.resumeWith(Result.success(Unit))
    }

    override fun error() {
        connect?.resumeWith(Result.failure(SocketConnectException()))
    }

    override fun readyForRead() {
        if (readData.continuation == null) {
            holder.key.removeListen(Selector.INPUT_READY)
            return
        }
        val readed = runCatching { channel.read(readData.data!!) }
        if (readData.full) {
            if (readData.data!!.remaining == 0 || readed.isFailure) {
                val con = readData.continuation!!
                readData.reset()
                con.resumeWith(readed)
                if (!holder.key.closed && readData.continuation == null) {
                    holder.key.removeListen(Selector.INPUT_READY)
                }
            }
        } else {
            val con = readData.continuation!!
            readData.reset()
            con.resumeWith(readed)
            if (!holder.key.closed && readData.continuation == null) {
                holder.key.removeListen(Selector.INPUT_READY)
            }
        }
    }

    override fun close() {
        check(!holder.key.closed) { "Connection already closed" }
        readData.continuation?.resumeWithException(SocketClosedException())
        sendData.continuation?.resumeWithException(SocketClosedException())
        readData.reset()
        sendData.reset()
        holder.key.listensFlag = 0
        holder.key.close()
        channel.close()
    }

    override suspend fun asyncClose() {
        close()
    }

    override suspend fun write(data: ByteBuffer): Int {
        val l = data.remaining
        if (l == 0) {
            return 0
        }

        if (sendData.continuation != null) {
            throw IllegalStateException("Connection already have write listener")
        } else {
            val wrote = channel.write(data)
            if (wrote == l) {
                return wrote
            }
        }
        sendData.data = data
        suspendCoroutine<Int> {
            sendData.continuation = it
            holder.key.addListen(Selector.OUTPUT_READY)
        }
        return l
    }

    override suspend fun flush() {

    }

    override val available: Int
        get() = -1

    override suspend fun readFully(dest: ByteBuffer): Int {
        if (dest.remaining == 0) {
            return 0
        }
        if (readData.continuation != null) {
            throw IllegalStateException("Connection already have read listener")
        }
        val r = channel.read(dest)
        if (r > 0 && dest.remaining == 0) {
            return r
        }
        readData.full = true
        val readed = suspendCoroutine<Int> {
            readData.continuation = it
            readData.data = dest
            holder.key.addListen(Selector.INPUT_READY)
        }
        if (readed <= 0) {
            throw SocketClosedException()
        }
        return readed
    }

    override suspend fun read(dest: ByteBuffer): Int {
        if (dest.remaining == 0) {
            return 0
        }
        if (readData.continuation != null) {
            throw IllegalStateException("Connection already have read listener")
        }
        val r = channel.read(dest)
        if (r > 0) {
            return r
        }
        readData.full = false
        val readed = suspendCoroutine<Int> {
            readData.continuation = it
            readData.data = dest
            holder.key.addListen(Selector.INPUT_READY)
        }
        if (readed <= 0) {
            throw SocketClosedException()
        }
        return readed
    }
}