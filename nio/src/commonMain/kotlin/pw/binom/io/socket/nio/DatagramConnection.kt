package pw.binom.io.socket.nio

import pw.binom.ByteBuffer
import pw.binom.concurrency.ConcurrentQueue
import pw.binom.concurrency.ThreadRef
import pw.binom.doFreeze
import pw.binom.io.socket.DatagramChannel
import pw.binom.io.socket.MutableNetworkAddress
import pw.binom.io.socket.NetworkAddress
import pw.binom.io.socket.SocketSelector
import pw.binom.popOrNull
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DatagramConnection(val manager: SocketNIOManager) :
    AbstractConnection() {

    class SendData {
        var addr: NetworkAddress? = null
        var data: ByteBuffer? = null
        var continuation: Continuation<Int>? = null
        fun reset() {
            data = null
            continuation = null
        }
    }

    class ReadData {
        var addr: MutableNetworkAddress? = null
        var data: ByteBuffer? = null
        var continuation: Continuation<Unit>? = null
        var full = false
        fun reset() {
            data = null
            continuation = null
        }
    }

    private var sendData = SendData()
    private var readData = ReadData()

    lateinit var holder: UdpHolder

    suspend fun write(data: ByteBuffer, address: NetworkAddress): Int {
        val l = data.remaining
        if (l == 0)
            return 0
        sendData.addr = address
        sendData.data = data
        val sendded = suspendCoroutine<Int> {
            sendData.continuation = it
            holder.key.listen(
                holder.key.listenReadable,
                true
            )
        }

        return sendded
    }

    suspend fun read(data: ByteBuffer, address: MutableNetworkAddress?): Boolean {
        if (data.remaining == 0) {
            return false
        }

        if (readData.continuation != null) {
            throw IllegalStateException("Connection already have read listener")
        }
        readData.addr = address
        readData.data = data
        readData.full = false
        suspendCoroutine<Unit> {
            readData.continuation = it
            holder.key.listen(true, holder.key.listenWritable)
        }
        return true
    }

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

        val result = runCatching { holder.channel.send(sendData.data!!, sendData.addr!!) }
        if (result.isFailure) {
            sendData.continuation!!.resumeWith(result)
            sendData.reset()
            return false
        }
        return if (sendData.data!!.remaining == 0) {
            sendData.continuation!!.resumeWith(result)
            sendData.reset()
            false
        } else {
            true
        }
    }

    override fun readyForRead(): Boolean {
        if (readData.continuation == null) {
            return false
        }
        val r = runCatching {
            holder.channel.receive(readData.data!!, readData.addr)
        }
        if (r.isFailure) {
            readData.continuation!!.resumeWith(r)
            readData.reset()
            return false
        }
        return if (readData.full) {
            if (readData.data!!.remaining == 0) {
                readData.continuation!!.resumeWith(r)
                readData.reset()
                false
            } else {
                true
            }
        } else {
            readData.continuation!!.resumeWith(r)
            false
        }
    }

    override fun close() {
        holder.key.cancel()
        holder.channel.close()
    }
}

class UdpHolder(val channel: DatagramChannel, val key: SocketSelector.SelectorKey) {
    internal val readyForWriteListener = ConcurrentQueue<() -> Unit>()
    private val networkThread = ThreadRef()

    fun waitReadyForWrite(func: () -> Unit) {
        if (networkThread.same) {
            func()
        } else {
            readyForWriteListener.push(func.doFreeze())
            key.listen(key.listenReadable, true)
        }
    }
}