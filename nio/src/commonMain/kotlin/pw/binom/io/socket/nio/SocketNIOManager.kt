package pw.binom.io.socket.nio

import pw.binom.ByteBuffer
import pw.binom.io.*
import pw.binom.io.socket.*
import pw.binom.neverFreeze
import pw.binom.printStacktrace
import pw.binom.start
import pw.binom.thread.Lock
import pw.binom.thread.synchronize
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

open class SocketNIOManager : Closeable {

    class ReadInterruptException : RuntimeException()
    interface ConnectHandler {
        fun clientConnected(connection: ConnectionRaw, manager: SocketNIOManager)
    }

    class IOSchedule(val buffer: ByteBuffer, val con: Continuation<Int>)

    inner class ConnectionRaw internal constructor(val manager: SocketNIOManager, internal val channel: SocketChannel, var attachment: Any?) : AsyncChannel {

        internal var readSchedule: IOSchedule? = null
        internal var writeSchedule: IOSchedule? = null

        /**
         * Waits when socket ready for read. When that moment come will call [func].
         * After call [func] lister will reset. And you must call [waitReadyForRead] again if you want
         * to wait read event again.
         *
         * @param func function for call when socket ready for read
         */
        fun waitReadyForRead(func: ((ConnectionRaw) -> Unit)?) {
            readyForReadListener = func
            if (func != null) {
                selectionKey.updateListening(true, selectionKey.listenWritable)
            }
        }

        fun waitReadyForWrite(func: ((ConnectionRaw) -> Unit)?) {
            readyForWriteListener = func
            if (func != null) {
                selectionKey.updateListening(selectionKey.listenReadable, true)
            }
        }

        internal var readyForReadListener: ((ConnectionRaw) -> Unit)? = null
        internal var readyForWriteListener: ((ConnectionRaw) -> Unit)? = null

        internal lateinit var selectionKey: SocketSelector.SelectorKey

        operator fun invoke(func: suspend (ConnectionRaw) -> Unit) {
            func.start(this)
        }

        internal var detached = false

        fun detach(): SocketChannel {
            val schedules = run {
                val w = writeSchedule
                val r = readSchedule
                writeSchedule = null
                readSchedule = null
                detached = true
                selectionKey.updateListening(false, false)
                if (!selectionKey.isCanlelled)
                    selectionKey.cancel()
                readyForReadListener = null
                w to r
            }
            schedules.first?.let {
                it.con.resumeWithException(RuntimeException("Connection Detached"))
            }
            schedules.second?.let {
                it.con.resumeWithException(RuntimeException("Connection Detached"))
            }
            return channel
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
                it.con.resumeWithException(ClosedException())
            }
            schedules.second?.let {
                it.con.resumeWithException(ClosedException())
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
                val wrote = this.channel.write(data)
                if (wrote == l) {
                    return wrote
                }
            }
            suspendCoroutine<Int> {
                writeSchedule = IOSchedule(data, it)
                selectionKey.updateListening(
                        selectionKey.listenReadable,
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
                readSchedule = IOSchedule(dest, it)
                selectionKey.updateListening(true, selectionKey.listenWritable)
            }
        }

        init {
            neverFreeze()
        }
    }

    private val selector = SocketSelector()

    @OptIn(ExperimentalTime::class)
    var updateTime = Duration.ZERO

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
            if (client.manager !== this)
                return

            if (client.selectionKey.isCanlelled) {
                return
            }
            if (client.detached)
                return

            val readReadyCallback = client.readyForReadListener
            if (key.isReadable && readReadyCallback != null) {
                client.readyForReadListener = null
                try {
                    readReadyCallback(client)
                } catch (e: Throwable) {
                    e.printStacktrace()
                    client.readSchedule?.con?.resumeWithException(e)
                    client.writeSchedule?.con?.resumeWithException(e)
                    client.readSchedule = null
                    client.writeSchedule = null
                    key.updateListening(false, false)
                    client.detach().close()
                    return
                }
            }

            val readReadyCallbackW = client.readyForWriteListener
            if (key.isWritable && readReadyCallbackW != null) {
                client.readyForWriteListener = null
                try {
                    readReadyCallbackW(client)
                } catch (e: Throwable) {
                    e.printStacktrace()
                    client.readSchedule?.con?.resumeWithException(e)
                    client.writeSchedule?.con?.resumeWithException(e)
                    client.readSchedule = null
                    client.writeSchedule = null
                    key.updateListening(false, false)
                    client.detach().close()
                    return
                }
            }

            val writeWait = client.writeSchedule

            if (key.isWritable && writeWait != null) {
                val result = runCatching { client.channel.write(writeWait.buffer) }
                if (result.isFailure) {
                    client.writeSchedule = null
                    writeWait.con.resumeWith(result)
                } else {
                    if (writeWait.buffer.remaining == 0) {
                        client.writeSchedule = null
                        writeWait.con.resumeWith(result)
                    }
                }
            }

            val readWait = client.readSchedule
            if (key.isReadable && readWait != null) {
                client.readSchedule = null
                readWait.con.resumeWith(runCatching { client.channel.read(readWait.buffer) })
            }

            if (!client.channel.isConnected) {
                key.cancel()
                return
            }

            if (!key.isCanlelled)
                key.updateListening(
                        client.readSchedule != null || client.readyForReadListener != null,
                        client.writeSchedule != null || client.readyForWriteListener != null
                )
        } catch (e: Throwable) {
            key.updateListening(false, false)
            client.readSchedule?.con?.resumeWithException(e)
            client.writeSchedule?.con?.resumeWithException(e)
            client.readSchedule = null
            client.writeSchedule = null
            client.detach().close()
        }
    }

    protected open fun processAccept(key: SocketSelector.SelectorKey) {
        val server = key.channel as ServerSocketChannel
        val cl = server.accept() ?: return@processAccept
        val connection = ConnectionRaw(channel = cl, attachment = null, manager = this)
        connection.channel.blocking = false
        connection.selectionKey = selector.reg(connection.channel, connection)
        val handler = key.attachment as ConnectHandler?
        handler?.clientConnected(connection, this)
    }

    @OptIn(ExperimentalTime::class)
    protected open fun processEvent(key: SocketSelector.SelectorKey) {
        updateTime += measureTime {
            if (key.channel is ServerSocketChannel) {
                processAccept(key)
            } else {
                processIo(key)
            }
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
            val connection = ConnectionRaw(channel = channel, attachment = attachment, manager = this)
            connection.selectionKey = selector.reg(channel, connection)
            connection.selectionKey.updateListening(false, false)
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

    init {
        neverFreeze()
    }
}