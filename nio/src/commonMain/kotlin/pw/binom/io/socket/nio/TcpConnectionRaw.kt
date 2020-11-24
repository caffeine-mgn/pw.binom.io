package pw.binom.io.socket.nio

import pw.binom.*
import pw.binom.concurrency.ConcurrentQueue
import pw.binom.concurrency.ThreadRef
import pw.binom.io.AsyncChannel
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import pw.binom.io.socket.ServerSocketChannel
import pw.binom.io.socket.SocketChannel
import pw.binom.io.socket.SocketSelector
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TcpConnectionRaw internal constructor(val holder: SocketHolder, var attachment: Any?) :
    AbstractConnection(), AsyncChannel {

//    internal var readSchedule: IOSchedule? = null
//    internal var writeSchedule: IOSchedule? = null

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
    private var readyForWrite = false

    init {
        neverFreeze()
    }

    operator fun invoke(func: suspend (TcpConnectionRaw) -> Unit) {
        func.start(this)
    }

    internal var detached = false

//    fun detach(): SocketHolder {
//        val schedules = run {
//            val w = writeSchedule
//            val r = readSchedule
//            writeSchedule = null
//            readSchedule = null
//            detached = true
//            holder.selectionKey.listen(false, false, false)
//            holder.readyForReadListener.clear()
//            holder.readyForWriteListener.clear()
//            w to r
//        }
//        schedules.first?.let {
//            it.finish(Result.failure(RuntimeException("Connection Detached")))
//        }
//        schedules.second?.let {
//            it.finish(Result.failure(RuntimeException("Connection Detached")))
//        }
//        return holder
//    }

//    internal fun forceClose() {
//        val schedules = run {
//            val w = writeSchedule
//            val r = readSchedule
//            writeSchedule = null
//            readSchedule = null
//            w to r
//        }
//        schedules.first?.let {
//            it.finish(Result.failure(ClosedException()))
//        }
//        schedules.second?.let {
//            it.finish(Result.failure(ClosedException()))
//        }
//        detach().close()
//    }

    override fun close() {
        holder.selectionKey.cancel()
        holder.channel.close()
    }

    override suspend fun asyncClose() {
        close()
    }

    override suspend fun write(data: ByteBuffer): Int {
        val l = data.remaining
        if (l == 0)
            return 0

        if (sendData.continuation != null) {
            throw IllegalStateException("Connection already have write listener")
        } else {
            val wrote = holder.channel.write(data)
            if (wrote == l) {
                return wrote
            }
        }
        suspendCoroutine<Int> {
            sendData.continuation = it

            holder.selectionKey.listen(
                holder.selectionKey.listenReadable,
                true
            )
        }
        return l
    }

    override suspend fun flush() {
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
        return suspendCoroutine {
            readData.continuation = it
            readData.data = dest
//            readSchedule = IOScheduleContinuation(dest, it)
            holder.selectionKey.listen(true, holder.selectionKey.listenWritable)
        }
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

        val result = runCatching { holder.channel.write(sendData.data!!) }
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
        val readed = runCatching { holder.channel.read(readData.data!!) }
        return if (readData.full) {
            if (readData.data!!.remaining == 0) {
                readData.continuation!!.resumeWith(readed)
                readData.reset()
                false
            } else {
                true
            }
        } else {
            readData.continuation!!.resumeWith(readed)
            false
        }
    }
}

class SocketHolder(internal val channel: SocketChannel) : Closeable {
    internal val readyForWriteListener = ConcurrentQueue<() -> Unit>()
//    internal val readyForWriteListener2 = ConcurrentQueue<ObjectTree<Continuation<Unit>>>()
    private val networkThread = ThreadRef()
    internal lateinit var selectionKey: SocketSelector.SelectorKey

//    suspend fun waitWrite() {
//        suspendCoroutine<Unit> {
//            readyForWriteListener2.push(ObjectTree.create(it))
//        }
//    }

    fun waitReadyForWrite(func: () -> Unit) {
        if (networkThread.same) {
            func()
        } else {
            readyForWriteListener.push(func.doFreeze())
            selectionKey.listen(selectionKey.listenReadable, true)
        }
    }

    override fun close() {
        if (!selectionKey.isCanlelled) {
            selectionKey.cancel()
        }
        channel.close()
    }
}