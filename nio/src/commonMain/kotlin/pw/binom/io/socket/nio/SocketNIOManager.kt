package pw.binom.io.socket.nio

import pw.binom.*
import pw.binom.io.*
import pw.binom.io.socket.*
import pw.binom.pool.DefaultPool
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class SocketNIOManager : Closeable {
    class ReadInterruptException : RuntimeException()
    interface ConnectHandler {
        fun clientConnected(connection: ConnectionRaw, manager: SocketNIOManager)
    }

    private val waitEventPool = DefaultPool(30) {
        WaitEvent()
    }

    internal class WaitEvent {
        lateinit var continuation: Continuation<Int>
        lateinit var data: ByteArray
        var offset: Int = 0
        var length: Int = 0
    }

    inner class ConnectionRaw internal constructor(val manager: SocketNIOManager, internal val channel: SocketChannel, var attachment: Any?) : AsyncChannel {
        internal lateinit var selectionKey: SocketSelector.SelectorKey

        internal var _inputAvailable: Boolean = false
        val inputAvailable: Boolean
            get() = _inputAvailable

        fun readInterrupt() {
            while (true) {
                val v = readWaitList.popOrNull() ?: break
                v.continuation.resumeWithException(ReadInterruptException())
            }
        }

        override val input: AsyncInputStream = object : AsyncInputStream {
            private val staticData = ByteArray(1)
            override suspend fun read(): Byte {
                if (read(staticData) != 1)
                    throw EOFException()
                return staticData[0]
            }

            override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
                if (detached)
                    throw IllegalStateException("Connection was detached")
                if (channel.available > 0) {
                    return channel.read(data, offset, length)
                }

                while (true) {
                    val readed = suspendCoroutine<Int> { v ->
                        val waitEvent = waitEventPool.borrow {
                            it.continuation = v
                            it.data = data
                            it.offset = offset
                            it.length = length
                        }
                        readWaitList.push(waitEvent)
                        selectionKey.listenReadable = true
                    }
                    if (readed >= 0) {
                        return readed
                    }
                }
            }

            override suspend fun close() {
            }
        }

        override val output: AsyncOutputStream = object : AsyncOutputStream {
            override suspend fun write(data: Byte): Boolean =
                    write(ByteArray(1) { data }) == 1

            override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
                check(!detached) { "Connection was detached" }
                var len = length
                var off = offset
                while (len > 0) {
                    val r = suspendCoroutine<Int> { v ->
                        val waitEvent = waitEventPool.borrow {
                            it.continuation = v
                            it.data = data
                            it.offset = off
                            it.length = len
                        }
                        writeWaitList.push(waitEvent)
                        selectionKey.listenWritable = true
                    }
                    off += r
                    len -= r
                }
                return length
            }

            override suspend fun close() {
            }

            override suspend fun flush() {
            }
        }

        internal val readWaitList = Stack<WaitEvent>().asLiFoQueue()
        internal val writeWaitList = Stack<WaitEvent>().asLiFoQueue()

        operator fun invoke(func: suspend (ConnectionRaw) -> Unit) {
            func.start(this)
        }

        internal var detached = false

        fun detach(): SocketChannel {
            detached = true
            if (!selectionKey.isCanlelled)
                selectionKey.cancel()
            while (!readWaitList.isEmpty) {
                val e = readWaitList.pop()
                e.continuation.resumeWithException(RuntimeException("Connection Detached"))
                waitEventPool.recycle(e)
            }
            while (!writeWaitList.isEmpty) {
                val e = writeWaitList.pop()
                e.continuation.resumeWithException(RuntimeException("Connection Detached"))
                waitEventPool.recycle(e)
            }
            return channel
        }

        override suspend fun close() {
            while (!writeWaitList.isEmpty)
                writeWaitList.pop().continuation.resumeWithException(ClosedException())
            while (!readWaitList.isEmpty)
                readWaitList.pop().continuation.resumeWithException(ClosedException())
            detach().close()
        }

        init {
            neverFreeze()
        }
    }

    private val selector = SocketSelector(100)

    private val popResult = PopResult<WaitEvent>()

    fun update(timeout: Int? = null) = selector.process(timeout) {
        if (it.channel is ServerSocketChannel) {
            val server = it.channel as ServerSocketChannel
            val cl = server.accept() ?: return@process
            val connection = ConnectionRaw(channel = cl, attachment = null, manager = this)
            connection.channel.blocking = false
            connection.selectionKey = selector.reg(connection.channel, connection)
            val handler = it.attachment as ConnectHandler?
            handler?.clientConnected(connection, this)
        } else {
            val client = (it.attachment as ConnectionRaw?)
            if (client?.detached == true) {
                TODO("Client is detached")
            }
            if (client == null) {
                return@process
            }
            if (client.manager !== this)
                return@process

            if (client.selectionKey.isCanlelled) {
                TODO()
                return@process
            }

            var l = it.isReadable

            if (!client.selectionKey.isCanlelled && it.isWritable && client.selectionKey.attachment === client) {
                client.writeWaitList.pop(popResult)
                if (!popResult.isEmpty) {
                    val ev = popResult.value
                    try {
                        val wroteBytesCount = try {
                            client.channel.write(ev.data, ev.offset, ev.length)
                        } catch (e: Throwable) {
                            ev.continuation.resumeWithException(e)
                            return@process
                        }
                        ev.continuation.resume(wroteBytesCount)
                    } catch (e: Throwable) {
                        async {
                            client.close()
                        }
                        throw e
                    } finally {
                        waitEventPool.recycle(ev)
                    }
                }
            }

            if (!client.selectionKey.isCanlelled && it.isReadable && client.selectionKey.attachment === client) {
                client.readWaitList.pop(popResult)
                if (!popResult.isEmpty) {
                    val ev = popResult.value
                    try {
                        val readBytesCount = try {
                            l = false
                            client.channel.read(ev.data, ev.offset, ev.length)
                        } catch (e: Throwable) {
                            ev.continuation.resumeWithException(e)
                            return@process
                        }
                        ev.continuation.resume(readBytesCount)
                    } catch (e: Throwable) {
                        async {
                            client.close()
                        }
                        throw e
                    } finally {
                        waitEventPool.recycle(ev)
                    }
                }
            }

            client._inputAvailable = l

            if (!client.selectionKey.isCanlelled && client.selectionKey.attachment === client) {
                client.selectionKey.listenReadable = !client.readWaitList.isEmpty
                client.selectionKey.listenWritable = !client.writeWaitList.isEmpty
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
    fun attach(channel: SocketChannel, attachment: Any? = null): ConnectionRaw {
        channel.blocking = false
        val connection = ConnectionRaw(channel = channel, attachment = attachment, manager = this)
        connection.selectionKey = selector.reg(channel, connection)
        connection.selectionKey.listenReadable = false
        connection.selectionKey.listenWritable = false
        return connection
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