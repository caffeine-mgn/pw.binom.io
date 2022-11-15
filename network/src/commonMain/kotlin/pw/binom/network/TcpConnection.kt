package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.concurrency.SpinLock
import pw.binom.io.AsyncChannel
import pw.binom.io.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TcpConnection(channel: TcpClientSocketChannel) : AbstractConnection(), AsyncChannel {
    var connect: CancellableContinuation<Unit>? = null
    var description: String? = null
    private var closed = false
    private var _channel: TcpClientSocketChannel? = channel
    private val readLock = SpinLock()
    val channel: TcpClientSocketChannel
        get() = _channel!!
    val keys = KeyCollection()

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

    override fun readyForWrite(key: Selector.Key) {
        val key = this.keys
        if (key.isEmpty) {
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
            if (sendData.continuation == null) {
                key.setListensFlag(calcListenFlags())
            }
        } else {
            key.removeListen(Selector.OUTPUT_READY)
        }
    }

    override fun connecting() {
        if (error) {
            throw SocketConnectException()
        }
        this.keys.setListensFlag(Selector.EVENT_CONNECTED)
    }

    override fun connected() {
        val connect = connect
        this.connect = null
        this.keys.removeListen(Selector.EVENT_CONNECTED)
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

    override fun readyForRead(key: Selector.Key) {
        val keys = this.keys
        val continuation = readData.continuation
        val data = readData.data
        if (continuation == null) {
            keys.removeListen(Selector.INPUT_READY)
            return
        }
        data ?: error("readData.data is not set")
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
                if (readData.continuation == null) {
                    keys.removeListen(Selector.INPUT_READY)
                }
            }
        } else {
            val con = continuation
            readData.reset()
            con.resume(readed)
            if (readData.continuation == null) {
                keys.removeListen(Selector.INPUT_READY)
            }
        }
    }

    override fun close() {
        try {
            keys.close()
            _channel?.close()
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
        keys.checkEmpty()
        val oldRemaining = data.remaining
        if (oldRemaining == 0) {
            return 0
        }
        if (sendData.continuation != null) {
            error("Connection already has write operation")
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
//        lastWriteStackTrace = Throwable().stackTraceToString()
        return suspendCancellableCoroutine<Int> {
            sendData.continuation = it
            this.keys.addListen(Selector.OUTPUT_READY)
            this.keys.wakeup()
            it.invokeOnCancellation {
                this.keys.removeListen(Selector.OUTPUT_READY)
                this.keys.wakeup()
                sendData.reset()
            }
        }
    }

    override suspend fun flush() {
    }

    override val available: Int
        get() = -1

    override suspend fun readFully(dest: ByteBuffer): Int {
        keys.checkEmpty()
        if (dest.remaining == 0) {
            return 0
        }
        if (readData.continuation != null) {
            error("Connection already have read listener")
        }
        val r = channel.read(dest)
        if (r > 0 && dest.remaining == 0) {
            return r
        }
        readData.full = true
        val readed = suspendCancellableCoroutine<Int> {
            it.invokeOnCancellation {
                this.keys.removeListen(Selector.INPUT_READY)
                this.keys.wakeup()
                readData.continuation = null
                readData.data = null
            }
            readData.continuation = it
            readData.data = dest
            this.keys.addListen(Selector.INPUT_READY)
            this.keys.wakeup()
        }
        if (readed == 0) {
            runCatching { channel.close() }
            throw SocketClosedException()
        }
        return readed
    }

    override suspend fun read(dest: ByteBuffer): Int {
        keys.checkEmpty()
        if (readData.continuation != null) {
            error("Connection already have read listener")
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
            it.invokeOnCancellation {
                this.keys.removeListen(Selector.INPUT_READY)
                this.keys.wakeup()
                readData.continuation = null
                readData.data = null
            }
            readData.continuation = it
            readData.data = dest
            this.keys.addListen(Selector.INPUT_READY)
            this.keys.wakeup()
        }
//        if (readed == 0) {
//            runCatching { channel.close() }
//            throw SocketClosedException()
//        }
        return readed
    }
}
