package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.io.AsyncChannel
import pw.binom.io.ByteBuffer
import pw.binom.neverFreeze
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TcpConnection(channel: TcpClientSocketChannel) : AbstractConnection(), AsyncChannel {
    var connect: CancellableContinuation<Unit>? = null
    var description: String? = null
    private var closed = false
    private var _channel: TcpClientSocketChannel? = channel
    val channel: TcpClientSocketChannel
        get() = _channel!!
    var key: Selector.Key
        get() = _key!!
        set(value) {
            _key = value
        }
    private var _key: Selector.Key? = null

    override fun toString(): String =
        if (description == null) {
            "TcpConnection"
        } else {
            "TcpConnection($description)"
        }

    private val readData = object {
        var continuation: CancellableContinuation<Int>? = null
        var data: ByteBuffer? = null
        var full = false

        fun reset() {
            continuation = null
            data = null
        }
    }
    private val sendData = object {
        var continuation: CancellableContinuation<Int>? = null
        var data: ByteBuffer? = null
        fun reset() {
            continuation = null
            data = null
        }
    }

    private fun calcListenFlags() =
        when {
            readData.continuation != null && (sendData.continuation != null) -> Selector.INPUT_READY or Selector.OUTPUT_READY
            readData.continuation != null -> Selector.INPUT_READY
            sendData.continuation != null -> Selector.OUTPUT_READY
            else -> 0
        }

    override fun readyForWrite() {
        val key = _key
        if (key == null) {
            println("illegal readyForWrite calling. connection already closed")
            return
        }

        if (sendData.continuation != null) {
            val result = runCatching { channel.write(sendData.data!!) }
            if (result.isSuccess && result.getOrNull()!! < 0) {
                sendData.continuation?.cancel(SocketClosedException())
                sendData.reset()
                return
            }
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
        runCatching { this.channel.close() }
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
        val key = _key
        if (key == null) {
            println("illegal readyForRead calling. connection already closed")
            return
        }
        val continuation = readData.continuation
        val data = readData.data
        if (continuation == null) {
            if (!key.closed) {
                key.removeListen(Selector.INPUT_READY)
            }
            return
        }
        data ?: throw IllegalStateException("readData.data is not set")
        val readed = channel.read(data)
        if (readed == -1) {
            val c = continuation
            readData.reset()
            close()
            c.resumeWithException(SocketClosedException())
            return
        }
        if (readData.full) {
            if (data.remaining == 0) {
                val con = continuation
                readData.reset()
                con.resume(readed)
                if (!key.closed && readData.continuation == null) {
                    key.removeListen(Selector.INPUT_READY)
                }
            }
        } else {
            val con = continuation
            readData.reset()
            con.resume(readed)
            if (!key.closed && readData.continuation == null) {
                key.removeListen(Selector.INPUT_READY)
            }
        }
    }

    override fun close() {
        if (_key != null && key.closed) {
            return
        }
        try {
            if (!key.closed) {
                key.listensFlag = 0
                key.close()
            }
            channel.close()
            _key = null
            _channel = null
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
        val oldRemaining = data.remaining
        if (oldRemaining == 0) {
            return 0
        }
        if (sendData.continuation != null) {
            throw IllegalStateException("Connection already have write listener")
        }
        val wrote = channel.write(data)
        if (wrote > 0) {
            return wrote
        }
        if (wrote == -1) {
            close()
            throw SocketClosedException()
        }
        sendData.data = data
        return suspendCancellableCoroutine<Int> {
            sendData.continuation = it
            key.addListen(Selector.OUTPUT_READY)
            it.invokeOnCancellation {
                sendData.continuation = null
                sendData.data = null
            }
        }
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
            it.invokeOnCancellation {
                readData.continuation = null
                readData.data = null
            }
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
        if (r == -1) {
            channel.close()
            throw SocketClosedException()
        }
        readData.full = false
        val readed = suspendCancellableCoroutine<Int> {
            readData.continuation = it
            readData.data = dest
            key.addListen(Selector.INPUT_READY)
            it.invokeOnCancellation {
                if (!key.closed) {
                    key.removeListen(Selector.INPUT_READY)
                }
                readData.continuation = null
                readData.data = null
            }
        }
//        if (readed == 0) {
//            runCatching { channel.close() }
//            throw SocketClosedException()
//        }
        return readed
    }

    init {
        neverFreeze()
    }
}
