package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.io.AsyncChannel
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TcpConnection(
    val channel: TcpClientSocket,
    private val currentKey: SelectorKey
) : AbstractConnection(), AsyncChannel {
    private var connect: CancellableContinuation<Unit>? = null
    var description: String? = null

    override fun toString(): String =
        if (description == null) {
            "TcpConnection"
        } else {
            "TcpConnection($description)"
        }

    private class IOState {
        var continuation: CancellableContinuation<Int>? = null
        var data: ByteBuffer? = null
        var full = false

        fun reset() {
            continuation = null
            data = null
        }

        fun set(continuation: CancellableContinuation<Int>, data: ByteBuffer) {
            this.continuation = continuation
            this.data = data
        }

        fun cancel(throwable: Throwable? = null) {
            continuation?.cancel(throwable)
            continuation = null
            data = null
        }
    }

    private val readData = IOState()
    private val sendData = IOState()

    private fun calcListenFlags() =
        when {
            readData.continuation != null && (sendData.continuation != null) -> KeyListenFlags.READ or KeyListenFlags.WRITE
            readData.continuation != null -> KeyListenFlags.READ
            sendData.continuation != null -> KeyListenFlags.WRITE
            else -> 0
        }

    override fun readyForWrite(key: SelectorKey) {
        if (checkConnect()) {
            return
        }
        if (sendData.continuation != null) {
            val result = runCatching { channel.send(sendData.data!!) }
            if (result.isSuccess && result.getOrNull()!! < 0) {
                sendData.continuation?.cancel(SocketClosedException())
                sendData.reset()
                return
            }
            if (sendData.data!!.remaining == 0) {
                val con = sendData.continuation!!
                sendData.reset()
//                key.removeListen(KeyListenFlags.WRITE)
                con.resumeWith(result)
            } else {
                key.addListen(KeyListenFlags.WRITE)
            }
            if (sendData.continuation == null) {
                currentKey.listenFlags = calcListenFlags()
            }
        } else {
//            key.removeListen(KeyListenFlags.WRITE)
        }
    }

    override fun readyForRead(key: SelectorKey) {
        if (checkConnect()) {
            return
        }

        val continuation = readData.continuation
        val data = readData.data
        if (continuation == null) {
            currentKey.removeListen(KeyListenFlags.READ)
            return
        }
        data ?: error("readData.data is not set")
        val readed = channel.receive(data)
        if (readed == -1) {
            readData.reset()
            close()
            continuation.resumeWithException(SocketClosedException())
            return
        }
        if (readData.full) {
            if (data.remaining == 0) {
                readData.reset()
                continuation.resume(readed)
            } else {
                currentKey.addListen(KeyListenFlags.READ)
            }
        } else {
            readData.reset()
            continuation.resume(readed)
        }
    }

    override fun error() {
        val connect = this.connect
        if (connect != null) {
            this.connect = null
            close()
            connect.resumeWithException(SocketConnectException())
            return
        }
        error = true

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

    override suspend fun connection() {
        val connect = this.connect
        check(connect == null) { "Connection already try connect" }
        try {
            suspendCancellableCoroutine<Unit> {
                it.invokeOnCancellation {
                    this.connect = null
                    this.currentKey.listenFlags = 0
                    this.currentKey.selector.wakeup()
                }

                try {
                    this.connect = it
                    this.currentKey.listenFlags = KeyListenFlags.WRITE or KeyListenFlags.ERROR
                    this.currentKey.selector.wakeup()
                } catch (e: Throwable) {
                    this.connect = null
                    this.currentKey.listenFlags = 0
                    it.resumeWithException(e)
                }
            }
        } catch (e: Throwable) {
            throw e
        }
    }

    private var error = false

    private fun checkConnect(): Boolean {
        val connect = this.connect
        if (connect != null) {
            this.connect = null
            connect.resume(Unit)
            return true
        }
        return false
    }

    override fun close() {
        try {
            currentKey.close()
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
        println("writing...")
        val oldRemaining = data.remaining
        if (oldRemaining == 0) {
            return 0
        }
        if (sendData.continuation != null) {
            error("Connection already has write operation")
        }
        val wrote = channel.send(data)
        println("wrote: $wrote")
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
            this.currentKey.addListen(KeyListenFlags.WRITE)
            this.currentKey.selector.wakeup()
            it.invokeOnCancellation {
                this.currentKey.removeListen(KeyListenFlags.WRITE)
                this.currentKey.selector.wakeup()
                sendData.reset()
            }
        }
    }

    override suspend fun flush() {
        // Do nothing
    }

    override val available: Int
        get() = -1

    override suspend fun readFully(dest: ByteBuffer): Int {
        if (dest.remaining == 0) {
            return 0
        }
        if (readData.continuation != null) {
            error("Connection already have read listener")
        }
        val r = channel.receive(dest)
        if (r > 0 && dest.remaining == 0) {
            return r
        }
        if (r == -1) {
            channel.close()
            throw SocketClosedException()
        }
        readData.full = true
        val readed = suspendCancellableCoroutine<Int> { continuation ->
            continuation.invokeOnCancellation {
                currentKey.removeListen(KeyListenFlags.READ)
                readData.reset()
                currentKey.selector.wakeup()
            }
            readData.set(
                continuation = continuation,
                data = dest
            )
            currentKey.addListen(KeyListenFlags.READ)
            currentKey.selector.wakeup()
        }
        if (readed == 0) {
            channel.closeAnyway()
            throw SocketClosedException()
        }
        return readed
    }

    override suspend fun read(dest: ByteBuffer): Int {
        if (dest.remaining == 0) {
            return 0
        }
        check(readData.continuation == null) { "Connection already have read listener" }

        val r = try {
            channel.receive(dest)
        } catch (e: Throwable) {
            throw e
        }
        if (r > 0) {
            return r
        }
        if (r == -1) {
            channel.close()
            throw SocketClosedException()
        }
        readData.full = false
        return suspendCancellableCoroutine {
            it.invokeOnCancellation {
                currentKey.removeListen(KeyListenFlags.READ)
                readData.reset()
                currentKey.selector.wakeup()
            }
            readData.set(
                continuation = it,
                data = dest
            )
            currentKey.addListen(KeyListenFlags.READ)
            currentKey.selector.wakeup()
        }
    }
}
