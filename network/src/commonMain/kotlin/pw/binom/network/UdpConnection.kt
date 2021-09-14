package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.CancelledException
import pw.binom.io.use
import pw.binom.popOrNull
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UdpConnection(val channel: UdpSocketChannel) : AbstractConnection() {

    companion object {
        fun randomPort() = UdpSocketChannel().use {
            it.bind(NetworkAddress.Immutable(host = "127.0.0.1", port = 0))
            it.port!!
        }
    }

    lateinit var key: Selector.Key

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
        if (sendData.continuation == null) {
            key.removeListen(Selector.OUTPUT_READY)
            return
        }

        val result = runCatching { channel.send(sendData.data!!, sendData.address!!) }
        if (sendData.data!!.remaining == 0) {
            val con = sendData.continuation!!
            sendData.reset()
            key.removeListen(Selector.OUTPUT_READY)
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
            key.removeListen(Selector.INPUT_READY)
            return
        }
        val readed = runCatching { channel.recv(readData.data!!, readData.address) }
        if (readData.full) {
            if (readData.data!!.remaining == 0) {
                val con = readData.continuation!!
                readData.reset()
                key.removeListen(Selector.INPUT_READY)
                con.resumeWith(readed)
            }
        } else {
            val con = readData.continuation!!
            readData.reset()
            key.removeListen(Selector.INPUT_READY)
            con.resumeWith(readed)
        }
    }

    override fun close() {
        readData.continuation?.resumeWithException(SocketClosedException())
        sendData.continuation?.resumeWithException(SocketClosedException())
        readData.reset()
        sendData.reset()
        key.listensFlag = 0
        key.close()
        channel.close()
    }

    fun interruptReading(): Boolean {
        val continuation = readData.continuation ?: return false
        continuation.resumeWithException(CancelledException())
        readData.reset()
        return true
    }

    suspend fun read(dest: ByteBuffer, address: NetworkAddress.Mutable?): Int {
        if (readData.continuation != null) {
            throw IllegalStateException("Connection already have read listener")
        }
        if (dest.remaining == 0) {
            return 0
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
            key.addListen(Selector.INPUT_READY)
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
            key.addListen(Selector.OUTPUT_READY)
        }
        return l
    }
}