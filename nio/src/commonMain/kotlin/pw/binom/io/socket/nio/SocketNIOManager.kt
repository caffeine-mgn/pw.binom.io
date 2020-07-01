package pw.binom.io.socket.nio

import pw.binom.ByteBuffer
import pw.binom.async
import pw.binom.io.*
import pw.binom.io.socket.*
import pw.binom.neverFreeze
import pw.binom.start
import pw.binom.thread.Lock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

abstract class SocketNIOManager() : Closeable {

    class ReadInterruptException : RuntimeException()
    interface ConnectHandler {
        fun clientConnected(connection: ConnectionRaw, manager: SocketNIOManager)
    }

    class Water(val buffer: ByteBuffer, val con: Continuation<Int>)

    inner class ConnectionRaw internal constructor(val manager: SocketNIOManager, internal val channel: SocketChannel, var attachment: Any?) : AsyncChannel {

        //        val input = packagePool.borrow()
//        val output = packagePool.borrow()
        var readWait: Water? = null
        var writeWait: Water? = null

        internal lateinit var selectionKey: SocketSelector.SelectorKey

        fun readInterrupt() {
//            fillBufWaiter?.let {
//                it.resumeWithException(ReadInterruptException())
//                fillBufWaiter = null
//            }

            readWait?.let {
                it.con.resumeWithException(ReadInterruptException())
                readWait = null
            }
        }

        //        var readWater2: WaitEvent2? = null
//        var flushWaiter: Continuation<Unit>? = null
//        var fillBufWaiter: Continuation<Unit>? = null
//        var writeWater2: WaitEvent2? = null

        operator fun invoke(func: suspend (ConnectionRaw) -> Unit) {
            func.start(this)
        }

        internal var detached = false

        suspend fun detach(): SocketChannel {
            detached = true
            selectionKey.updateListening(false, false)
            if (!selectionKey.isCanlelled)
                selectionKey.cancel()
//            fillBufWaiter?.let {
//                it.resumeWithException(RuntimeException("Connection Detached"))
//                fillBufWaiter = null
//            }
            writeWait?.let {
                it.con.resumeWithException(RuntimeException("Connection Detached"))
                writeWait = null
            }
            return channel
        }

        internal suspend fun forceClose() {
//            fillBufWaiter?.let {
//                it.resumeWithException(ClosedException())
//                flushWaiter = null
//            }

            writeWait?.let {
                it.con.resumeWithException(ClosedException())
                writeWait = null
            }
            readWait?.let {
                it.con.resumeWithException(ClosedException())
                readWait = null
            }
            detach().close()
        }

        override suspend fun close() {
            flush()
            forceClose()
//            readWater2?.let {
//                it.continuation.resumeWithException(ClosedException())
//                waitEventPool2.recycle(it)
//                readWater2 = null
//            }


//            writeWater2?.let {
//                it.continuation.resumeWithException(ClosedException())
//                waitEventPool2.recycle(it)
//                readWater2 = null
//            }

        }

        override suspend fun write(data: ByteBuffer): Int {
            val l = data.remaining
            if (l == 0)
                return 0
            suspendCoroutine<Int> {
                writeWait = Water(data, it)
                selectionKey.updateListening(
                        selectionKey.listenReadable,
                        true
                )
            }
            return l
        }

//        @OptIn(ExperimentalTime::class)
//        override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//            check(!detached) { "Connection was detached" }
////            println("try write")
//            output.write(data, offset, length)
////
//////            waitWrite += measureTime {
////            var len = length
////            var off = offset
////            while (len > 0) {
//////                val l = channel.write(data, off, len)
//////                if (l < 0)
//////                    TODO("send returns $l")
//////                if (l == 0) {
////                println("Try to write...")
////                val l = suspendCoroutine<Int> { v ->
////                    val waitEvent = waitEventPool2.borrow {
////                        it.continuation = v
////                        it.data = data
////                        it.offset = off
////                        it.length = len
////                    }
////                    writeWater2 = waitEvent
////                    selectionKey.updateListening(selectionKey.listenReadable, true)
////                }
////                println("Writed!")
//////                    break
//////                }
////                len -= l
////                off += l
////            }
////
//////            }
//            return length
//        }

        override suspend fun flush() {
//            val buf = bufferPool.borrow()
//            try {
//                while (true) {
//                    val l = output.read(buf)
//                    if (l <= 0)
//                        break
//                    val r = suspendCoroutine<Int> { v ->
//                        val waitEvent = waitEventPool2.borrow {
//                            it.continuation = v
//                            it.data = buf
//                            it.offset = 0
//                            it.length = l
//                        }
//                        writeWater2 = waitEvent
//                        selectionKey.updateListening(selectionKey.listenReadable, true)
//                    }
//                    if (r == 0)
//                        throw SocketClosedException()
//                }
//            } finally {
//                bufferPool.recycle(buf)
//            }
        }

        override suspend fun skip(length: Long): Long {
            TODO("Not yet implemented")
        }

//        override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//            if (detached)
//                throw IllegalStateException("Connection was detached")
////            println("try read")
//            while (true) {
//                val l = input.read(data, offset, length)
//                if (l > 0) {
//                    return l
//                }
//                suspendCoroutine<Unit> {
//                    fillBufWaiter = it
//                    selectionKey.updateListening(true, selectionKey.listenWritable)
//                }
//
////                val buf = bufferPool.borrow()
////                try {
////                    val r = suspendCoroutine<Int> { сontinuation ->
////                        val waitEvent = waitEventPool2.borrow {
////                            it.continuation = сontinuation
////                            it.data = buf
////                            it.offset = 0
////                            it.length = buf.size
////                        }
////                        readWater2 = waitEvent
////                        selectionKey.updateListening(true, selectionKey.listenWritable)
////                    }
////                    if (r == 0)
////                        throw SocketClosedException()
////                    input.write(buf, 0, r)
////                } finally {
////                    bufferPool.recycle(buf)
////                }
//            }
//            /*
//            inputFetchSize
//
//            waitEventPool2.borrow {
//                it.continuation = v
//                it.data = data
//                it.offset = offset
//                it.length = length
//            }
//
//
//            while (true) {
//                val readed = suspendCoroutine<Int> { v ->
//                    val waitEvent = waitEventPool2.borrow {
//                        it.continuation = v
//                        it.data = data
//                        it.offset = offset
//                        it.length = length
//                    }
//                    readWater2 = waitEvent
//                    selectionKey.updateListening(true, selectionKey.listenWritable)
//                }
//                if (readed >= 0) {
//                    return readed
//                }
//            }
//            */
//        }

        override suspend fun read(dest: ByteBuffer): Int {
            if (dest.remaining == 0) {
                return 0
            }
            return suspendCoroutine {
                readWait = Water(dest, it)
                selectionKey.updateListening(true, selectionKey.listenWritable)
            }
        }

        init {
            neverFreeze()
        }
    }

    private val selector = SocketSelector(100)

    @OptIn(ExperimentalTime::class)
    var updateTime = Duration.ZERO

    private val executeOnThread = ArrayList<suspend (ConnectionRaw) -> Unit>()
    private val executeThreadLock = Lock()

    protected open fun processIo(key: SocketSelector.SelectorKey) {
        val client = (key.attachment as ConnectionRaw?) ?: return
        if (client.detached) {
            return
//                    throw IOException("Client is detached")
        }
        if (client.manager !== this)
            return

        if (!client.channel.isConnected) {
            client.writeWait?.let {
                client.writeWait = null
                it.con.resumeWithException(SocketClosedException())
            }
            client.readWait?.let {
                client.readWait = null
                it.con.resumeWithException(SocketClosedException())
            }
//            client.writeWater2?.continuation?.resumeWithException(SocketClosedException())
//            client.readWater2?.continuation?.resumeWithException(SocketClosedException())
            client.channel.close()
            return
        }

        if (client.selectionKey.isCanlelled) {
            return
        }
        if (client.detached)
            return
        var needRead = client.selectionKey.listenReadable
        var needWrite = client.selectionKey.listenWritable || client.readWait != null

        val writeWait = client.writeWait

        if (key.isWritable) {
            if (writeWait == null) {
                needWrite = false
            } else {
                try {
                    client.channel.write(writeWait.buffer)
                    needWrite = writeWait.buffer.remaining > 0
                    if (writeWait.buffer.remaining == 0) {
                        client.writeWait = null
                        writeWait.con.resume(0)
                        needWrite = client.writeWait != null
                    } else {
                        needWrite = true
                    }
//                    if (client.output.readRemaining > 0) {
//                        val l = client.output.read(buf)
//                        client.channel.write(buf, 0, l)
//                    }
//
//                    if (client.output.readRemaining <= 0) {
//                        needWrite = true
//
//                        val waiter = client.flushWaiter
//                        client.flushWaiter = null
//                        waiter?.resume(Unit)
//                    } else {
//                        needWrite = true
//                    }
                } catch (e: Throwable) {
                    writeWait.con.resumeWithException(e)
                    needWrite = false
                } finally {
                }
            }
        }
        val readWait = client.readWait
        if (key.isReadable && readWait != null) {
            client.readWait = null
            try {
                readWait.con.resume(client.channel.read(readWait.buffer))
            } catch (e: Throwable) {
                readWait.con.resumeWithException(e)
                needRead = false
            } finally {
            }

            if (!needWrite && client.readWait != null)
                needWrite = true
        } else {
            needRead = false
        }
        /*
        if (key.isReadable && client.fillBufWaiter != null) {
            val buf = bufferPool.borrow()
            try {
                if (client.input.readRemaining <= 0) {
                    val l = client.channel.read(buf)
                    client.input.write(buf, 0, l)
                }

                if (client.input.readRemaining > 0) {
                    needRead = false

                    val waiter = client.fillBufWaiter
                    client.fillBufWaiter = null
                    waiter?.resume(Unit)
                }

            } catch (e: Throwable) {
                val waiter = client.fillBufWaiter
                client.fillBufWaiter = null
                waiter?.resumeWithException(e)
                needRead = false
            } finally {
                bufferPool.recycle(buf)
            }

            if (!needWrite && client.flushWaiter != null)
                needWrite = true
        } else {
            needRead = false
        }
        */
        if (!key.isCanlelled)
            key.updateListening(client.readWait != null, client.writeWait != null)
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
//        if (threadPool != null) {
//            threadPool.execute {
//                processEvent(key)
//            }
//        } else {
//
//        }
            processEvent(key)
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
        executeThreadLock.hold {
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
            val con = (it.attachment as? ConnectionRaw) ?: return@forEach
            if (con.manager === this) {
                async {
                    con.close()
                }
            }
        }
        selector.close()
    }

    init {
        neverFreeze()
    }
}