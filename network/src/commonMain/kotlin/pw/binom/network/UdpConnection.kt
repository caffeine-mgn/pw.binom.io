package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.popOrNull
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UdpConnection(val channel: UdpSocketChannel) : AbstractConnection() {

    lateinit var holder: CrossThreadKeyHolder

    private class ReadData {
        var continuation: Continuation<Int>? = null
        var data: ByteBuffer? = null
        var address: NetworkAddress.Mutable? = null
        var full = false

        fun reset() {
            continuation = null
            data = null
            address = null
        }
    }

    private class SendData {
        var continuation: Continuation<Int>? = null
        var data: ByteBuffer? = null
        var address: NetworkAddress? = null
        fun reset() {
            continuation = null
            data = null
            address = null
        }
    }

    private val readData = ReadData()
    private val sendData = SendData()

    fun bind(address: NetworkAddress) {
        channel.bind(address)
    }

    override fun readyForWrite() {
        while (true) {
            val waiter = holder.readyForWriteListener.popOrNull() ?: break
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
        }

        if (sendData.continuation == null) {
            holder.key.removeListen(Selector.OUTPUT_READY)
            return
        }

        val result = runCatching { channel.send(sendData.data!!, sendData.address!!) }
        if (sendData.data!!.remaining == 0) {
            val con = sendData.continuation!!
            sendData.reset()
            holder.key.removeListen(Selector.OUTPUT_READY)
            con.resumeWith(result)
        }
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
        if (readData.continuation == null) {
            holder.key.removeListen(Selector.INPUT_READY)
            return
        }
        val readed = runCatching { channel.recv(readData.data!!, readData.address) }
        if (readData.full) {
            if (readData.data!!.remaining == 0) {
                val con = readData.continuation!!
                readData.reset()
                holder.key.removeListen(Selector.INPUT_READY)
                con.resumeWith(readed)
            }
        } else {
            val con = readData.continuation!!
            readData.reset()
            holder.key.removeListen(Selector.INPUT_READY)
            con.resumeWith(readed)
        }
    }

    override fun close() {
        readData.continuation?.resumeWithException(SocketClosedException())
        sendData.continuation?.resumeWithException(SocketClosedException())
        readData.reset()
        sendData.reset()
        holder.key.listensFlag = 0
        holder.key.close()
        channel.close()
    }

    suspend fun read(dest: ByteBuffer, address: NetworkAddress.Mutable?): Int {
        if (dest.remaining == 0) {
            return 0
        }
        if (readData.continuation != null) {
            throw IllegalStateException("Connection already have read listener")
        }
        val r = channel.recv(dest, address)
        if (r > 0) {
            return r
        }
        readData.full = false
        val readed = suspendCoroutine<Int> {
            readData.continuation = it
            readData.data = dest
            readData.address = address
            holder.key.addListen(Selector.INPUT_READY)
        }
        if (readed < 0) {
            throw SocketClosedException()
        }
        return readed
    }

    suspend fun write(data: ByteBuffer, address: NetworkAddress): Int {
        val l = data.remaining
        if (l == 0) {
            return 0
        }

        if (sendData.continuation != null) {
            throw IllegalStateException("Connection already have write listener")
        }
        val wrote = channel.send(data, address)
        if (wrote == l) {
            return wrote
        }

        sendData.data = data
        sendData.address = address
        suspendCoroutine<Int> {
            sendData.continuation = it
            holder.key.addListen(Selector.OUTPUT_READY)
        }
        return l
    }
}