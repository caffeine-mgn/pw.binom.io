package pw.binom.io.socket.nio

import pw.binom.*
import pw.binom.atomic.AtomicReference
import pw.binom.io.*
import pw.binom.io.socket.*
import pw.binom.concurrency.Lock
import pw.binom.thread.synchronize
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine
import kotlin.time.ExperimentalTime

open class SocketNIOManager : Closeable {

    class ReadInterruptException : RuntimeException()
    interface ConnectHandler {
        fun clientConnected(connection: ConnectionRaw, manager: SocketNIOManager)
    }

    abstract class IOSchedule(val buffer: ByteBuffer) {
        abstract fun finish(value: Result<Int>)
    }

    class IOScheduleContinuation(buffer: ByteBuffer, val con: Continuation<Int>) : IOSchedule(buffer) {
        override fun finish(value: Result<Int>) {
            con.resumeWith(value)
        }
    }

    class SocketHolder(internal val channel: SocketChannel) : Closeable {
        internal var readyForReadListener by AtomicReference<(() -> Unit)?>(null)
        internal var readyForWriteListener by AtomicReference<(() -> Unit)?>(null)
        internal lateinit var selectionKey: SocketSelector.SelectorKey

        /**
         * Waits when socket ready for read. When that moment come will call [func].
         * After call [func] lister will reset. And you must call [waitReadyForRead] again if you want
         * to wait read event again.
         *
         * @param func function for call when socket ready for read
         */
        fun waitReadyForRead(func: (() -> Unit)?) {
            readyForReadListener = func?.doFreeze()
            if (func != null) {
                selectionKey.listen(true, selectionKey.listenWritable)
            }
        }

        fun waitReadyForWrite(func: (() -> Unit)?) {
            readyForWriteListener = func?.doFreeze()
            if (func != null) {
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

    class ConnectionRaw internal constructor(val holder: SocketHolder, var attachment: Any?) : AsyncChannel {

        internal var readSchedule: IOSchedule? = null
        internal var writeSchedule: IOSchedule? = null

        init {
            neverFreeze()
        }

        operator fun invoke(func: suspend (ConnectionRaw) -> Unit) {
            func.start(this)
        }

        internal var detached = false

        fun detach(): SocketHolder {
            val schedules = run {
                val w = writeSchedule
                val r = readSchedule
                writeSchedule = null
                readSchedule = null
                detached = true
                holder.selectionKey.listen(false, false)
                holder.readyForReadListener = null
                w to r
            }
            schedules.first?.let {
                it.finish(Result.failure(RuntimeException("Connection Detached")))
            }
            schedules.second?.let {
                it.finish(Result.failure(RuntimeException("Connection Detached")))
            }
            return holder
        }

        internal fun forceClose() {
            val schedules = run {
                val w = writeSchedule
                val r = readSchedule
                writeSchedule = null
                readSchedule = null
                w to r
            }
            schedules.first?.let {
                it.finish(Result.failure(ClosedException()))
            }
            schedules.second?.let {
                it.finish(Result.failure(ClosedException()))
            }
            detach().close()
        }

        override suspend fun close() {
            forceClose()
        }

        override suspend fun write(data: ByteBuffer): Int {
            val l = data.remaining
            if (l == 0)
                return 0

            if (writeSchedule != null) {
                throw IllegalStateException("Connection already have write listener")
            } else {
                val wrote = holder.channel.write(data)
                if (wrote == l) {
                    return wrote
                }
            }
            suspendCoroutine<Int> {
                writeSchedule = IOScheduleContinuation(data, it)
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
            if (readSchedule != null) {
                throw IllegalStateException("Connection already have read listener")
            }
            return suspendCoroutine {
                readSchedule = IOScheduleContinuation(dest, it)
                holder.selectionKey.listen(true, holder.selectionKey.listenWritable)
            }
        }
    }

    private val selector = SocketSelector()

    val keys: Collection<SocketSelector.SelectorKey>
        get() = selector.keys

    private val executeOnThread = ArrayList<suspend (ConnectionRaw) -> Unit>()
    private val executeThreadLock = Lock()

    protected open fun processIo(key: SocketSelector.SelectorKey) {
        val client = (key.attachment as ConnectionRaw?) ?: return
        try {
            if (client.detached) {
                return
            }

            if (client.holder.selectionKey.isCanlelled) {
                return
            }
            if (client.detached)
                return

            val readReadyCallback = client.holder.readyForReadListener
            if (key.isReadable && readReadyCallback != null) {
                client.holder.readyForReadListener = null
                try {
                    readReadyCallback()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    client.readSchedule?.finish(Result.failure(e))
                    client.writeSchedule?.finish(Result.failure(e))
                    client.readSchedule = null
                    client.writeSchedule = null
                    key.listen(false, false)
                    client.detach().close()
                    return
                }
            }

            val readReadyCallbackW = client.holder.readyForWriteListener
            if (key.isWritable && readReadyCallbackW != null) {
                client.holder.readyForWriteListener = null
                try {
                    readReadyCallbackW()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    client.readSchedule?.finish(Result.failure(e))
                    client.writeSchedule?.finish(Result.failure(e))
                    client.readSchedule = null
                    client.writeSchedule = null
                    key.listen(false, false)
                    client.detach().close()
                    return
                }
                return
            }

            val writeWait = client.writeSchedule

            if (key.isWritable && writeWait != null) {
                val result = runCatching { client.holder.channel.write(writeWait.buffer) }
                if (result.isFailure) {
                    client.writeSchedule = null
                    writeWait.finish(result)
                } else {
                    if (writeWait.buffer.remaining == 0) {
                        client.writeSchedule = null
                        writeWait.finish(result)
                    }
                }
                return
            }

            val readWait = client.readSchedule
            if (key.isReadable && readWait != null) {
                client.readSchedule = null
                readWait.finish(runCatching { client.holder.channel.read(readWait.buffer) })
                return
            }

            if (!client.holder.channel.isConnected) {
                println("Connection not connected! Close Socket!")
                key.cancel()
                return
            }

            if (!key.isCanlelled)
                key.listen(
                        client.readSchedule != null || client.holder.readyForReadListener != null,
                        client.writeSchedule != null || client.holder.readyForWriteListener != null
                )
        } catch (e: Throwable) {
            println("ERROR!!! #2")
            e.printStackTrace()
            key.listen(false, false)
            client.readSchedule?.finish(Result.failure(e))
            client.writeSchedule?.finish(Result.failure(e))
            client.readSchedule = null
            client.writeSchedule = null
            client.detach().close()
        }
    }

    protected open fun processAccept(key: SocketSelector.SelectorKey) {
        val server = key.channel as ServerSocketChannel
        val cl = server.accept() ?: return@processAccept
        val holder = SocketHolder(cl)
        val connection = ConnectionRaw(attachment = null, holder = holder)
        cl.blocking = false
        val selectionKey = selector.reg(cl, connection)
        holder.selectionKey = selectionKey
        holder.doFreeze()
        val handler = key.attachment as ConnectHandler?
        handler?.clientConnected(connection, this)
    }

    @OptIn(ExperimentalTime::class)
    protected open fun processEvent(key: SocketSelector.SelectorKey) {
        if (key.channel is ServerSocketChannel) {
            processAccept(key)
        } else {
            processIo(key)
        }
    }

    @OptIn(ExperimentalTime::class)
    fun update(timeout: Int? = null) = waitEvents(timeout)

    protected fun waitEvents(timeout: Int?) {
        selector.process(timeout) { key ->
            try {
                processEvent(key)
            } catch (e: SocketClosedException) {
                //ignore disconnect event
            }
        }
    }

    fun findClient(func: (ConnectionRaw) -> Boolean): ConnectionRaw? =
            selector.keys.asSequence().map { it.attachment as? ConnectionRaw }.filterNotNull().find(func)

    /**
     * Returns clients count
     */
    val clientSize: Int
        get() = selector.keys.size

    fun bind(host: String = "0.0.0.0", port: Int, handler: ConnectHandler, factory: SocketFactory = SocketFactory.rawSocketFactory): ServerSocketChannel {
        val channel = factory.createSocketServerChannel()
        try {
            channel.blocking = false
            channel.bind(host, port)
            selector.reg(channel, handler)
            return channel
        } catch (e: BindException) {
            channel.close()
            throw e
        }
    }

    fun connect(host: String, port: Int, attachment: Any? = null, factory: SocketFactory = SocketFactory.rawSocketFactory): ConnectionRaw {
        val channel = factory.createSocketChannel()
        try {
            channel.connect(host, port)
            return attach(channel, attachment)
        } catch (e: ConnectException) {
            channel.close()
            throw e
        }
    }

    /**
     * Attach channel to current ConnectionManager
     */
    fun attach(channel: SocketChannel, attachment: Any? = null, func: (suspend (ConnectionRaw) -> Unit)? = null): ConnectionRaw {
        executeThreadLock.synchronize {
            channel.blocking = false
            val holder = SocketHolder(channel)
            val connection = ConnectionRaw(attachment = attachment, holder = holder)
            val selectionKey = selector.reg(channel, connection)
            holder.selectionKey = selectionKey
            holder.doFreeze()
            selectionKey.listen(false, false)
            if (func != null)
                executeOnThread += func
            return connection
        }
    }

    override fun close() {
        selector.keys.toTypedArray().forEach {
            it.cancel()
            it.channel.close()
//            val con = (it.attachment as? ConnectionRaw) ?: return@forEach
//            if (con.manager === this) {
//                async {
//                    con.close()
//                }
//            }
        }
        selector.close()
    }
}