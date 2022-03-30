package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.use
import kotlin.coroutines.resumeWithException

class UdpConnection(val channel: UdpSocketChannel) : AbstractConnection() {

    companion object {
        fun randomPort() = UdpSocketChannel().use {
            it.bind(NetworkAddress.Immutable(host = "127.0.0.1", port = 0))
            it.port!!
        }
    }

    lateinit var key: Selector.Key

    private class ReadData {
        var continuation: CancellableContinuation<Int>? = null
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
        var continuation: CancellableContinuation<Int>? = null
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

    val port
        get() = channel.port

    override fun readyForWrite() {
        if (sendData.continuation == null) {
            key.removeListen(Selector.OUTPUT_READY)
            return
        }

        val result = runCatching { channel.send(sendData.data!!, sendData.address!!) }
        if (result.isFailure) {
            val con = sendData.continuation!!
            sendData.reset()
            key.removeListen(Selector.OUTPUT_READY)
            con.resumeWithException(IOException("Can't send data."))
        } else {
            if (result.getOrNull()!! <= 0) {
                return
            }
        }
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

    override fun cancelSelector() {
        sendData.continuation?.cancel()
        sendData.continuation = null
        sendData.data = null
        readData.continuation?.cancel()
        readData.continuation = null
        readData.data = null
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
        val readed = suspendCancellableCoroutine<Int> {
            readData.continuation = it
            readData.data = dest
            readData.address = address
            key.addListen(Selector.INPUT_READY)
            it.invokeOnCancellation {
                readData.continuation = null
                readData.data = null
                readData.address = null
                key.removeListen(Selector.INPUT_READY)
            }
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
        suspendCancellableCoroutine<Int> {
            sendData.continuation = it
            key.addListen(Selector.OUTPUT_READY)
        }
        return l
    }
}
