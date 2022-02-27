package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.ByteBuffer
import pw.binom.io.AsyncChannel
import pw.binom.neverFreeze
import kotlin.coroutines.resumeWithException

class TcpConnection(val channel: TcpClientSocketChannel) : AbstractConnection(), AsyncChannel {

    var connect: CancellableContinuation<Unit>? = null
    var description: String? = null
    lateinit var key: Selector.Key

    override fun toString(): String =
        if (description == null) {
            "TcpConnection"
        } else {
            "TcpConnection($description)"
        }

    private class ReadData {
        var continuation: CancellableContinuation<Int>? = null
        var data: ByteBuffer? = null
        var full = false

        fun reset() {
            continuation = null
            data = null
        }
    }

    private class SendData {
        var continuation: CancellableContinuation<Int>? = null
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
            readData.continuation != null && (sendData.continuation != null) -> Selector.INPUT_READY or Selector.OUTPUT_READY
            readData.continuation != null -> Selector.INPUT_READY
            sendData.continuation != null -> Selector.OUTPUT_READY
            else -> 0
        }

    override fun readyForWrite() {
        if (sendData.continuation != null) {
            val result = runCatching { channel.write(sendData.data!!) }
            if (sendData.data!!.remaining == 0) {
                val con = sendData.continuation!!
                sendData.reset()
                key.removeListen(Selector.OUTPUT_READY)
                con.resumeWith(result)
            }
            if (sendData.continuation == null && !key.closed) {
                key.listensFlag = calcListenFlags()
            }
        } else {
            if (!key.closed) {
                key.removeListen(Selector.OUTPUT_READY)
            }
        }
    }

    override fun connecting() {
        if (error) {
            throw SocketConnectException()
        }
        key.listensFlag = Selector.EVENT_CONNECTED
    }

    override fun connected() {
        val connect = connect
        this.connect = null
        key.removeListen(Selector.EVENT_CONNECTED)
        connect?.resumeWith(Result.success(Unit))
    }

    private var error = false
    override fun error() {
        error = true
        if (connect != null) {
            val e = SocketConnectException()
            connect?.resumeWith(Result.failure(e))
        }
        if (readData.continuation != null) {
            val c = readData.continuation
            readData.reset()
            c?.resumeWith(Result.failure(SocketClosedException()))
        }
        if (sendData.continuation != null) {
            val c = sendData.continuation
            sendData.reset()
            c?.resumeWith(Result.failure(SocketClosedException()))
        }
    }

    override fun cancelSelector() {
        sendData.continuation?.cancel()
        sendData.continuation = null
        sendData.data = null
        readData.continuation?.cancel()
        readData.continuation = null
        readData.data = null
        connect?.cancel()
        connect = null
    }

    override fun readyForRead() {
        if (readData.continuation == null) {
            key.removeListen(Selector.INPUT_READY)
            return
        }
        val readed = runCatching { channel.read(readData.data!!) }
        if (readData.full) {
            if (readData.data!!.remaining == 0 || readed.isFailure) {
                val con = readData.continuation!!
                readData.reset()
                con.resumeWith(readed)
                if (!key.closed && readData.continuation == null) {
                    key.removeListen(Selector.INPUT_READY)
                }
            }
        } else {
            val con = readData.continuation!!
            readData.reset()
            con.resumeWith(readed)
            if (!key.closed && readData.continuation == null) {
                key.removeListen(Selector.INPUT_READY)
            }
        }
    }

    override fun close() {
        check(!key.closed) { "Connection already closed" }
        try {
            key.listensFlag = 0
            key.close()
            channel.close()
        } finally {
            readData.continuation?.resumeWithException(SocketClosedException())
            sendData.continuation?.resumeWithException(SocketClosedException())
            readData.reset()
            sendData.reset()
        }
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
        }
        val wrote = channel.write(data)
        if (wrote == l) {
            return wrote
        }
        sendData.data = data
        suspendCancellableCoroutine<Int> {
            sendData.continuation = it
            key.addListen(Selector.OUTPUT_READY)
            it.invokeOnCancellation {
                key.removeListen(Selector.OUTPUT_READY)
            }
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
        val readed = suspendCancellableCoroutine<Int> {
            readData.continuation = it
            readData.data = dest
            key.addListen(Selector.INPUT_READY)
        }
        if (readed == 0) {
            runCatching { channel.close() }
            throw SocketClosedException()
        }
        return readed
    }

    override suspend fun read(dest: ByteBuffer): Int {
        if (readData.continuation != null) {
            throw IllegalStateException("Connection already have read listener")
        }
        if (dest.remaining == 0) {
            return 0
        }
        val r = channel.read(dest)
        if (r > 0) {
            return r
        }
        readData.full = false
        val readed = suspendCancellableCoroutine<Int> {
            readData.continuation = it
            readData.data = dest
            key.addListen(Selector.INPUT_READY)
            it.invokeOnCancellation {
                key.removeListen(Selector.INPUT_READY)
                readData.continuation = null
                readData.data = null
            }
        }
        if (readed == 0) {
            runCatching { channel.close() }
            throw SocketClosedException()
        }
        return readed
    }

    init {
        neverFreeze()
    }
}
