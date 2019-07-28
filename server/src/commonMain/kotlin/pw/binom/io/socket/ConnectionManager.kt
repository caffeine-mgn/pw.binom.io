package pw.binom.io.socket

import pw.binom.PopResult
import pw.binom.Stack
import pw.binom.io.*
import pw.binom.neverFreeze
import pw.binom.start
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class ConnectionManager : Closeable {

    interface ConnectHandler {
        fun clientConnected(connection: Connection, manager: ConnectionManager)
    }

    internal class WaitEvent(val continuation: Continuation<Int>, val data: ByteArray, val offset: Int, val length: Int)

    inner class Connection internal constructor(val manager: ConnectionManager, internal val channel: SocketChannel, var attachment: Any?) : Closeable {
        internal lateinit var selectionKey: SocketSelector.SelectorKey

        internal var _inputAvailable: Boolean = false
        val inputAvailable: Boolean
            get() = _inputAvailable

        val input: AsyncInputStream = object : AsyncInputStream {
            override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
                if (detached)
                    throw IllegalStateException("Connection was detached")
                if (channel.available > 0) {
                    return channel.read(data, offset, length)
                }

                return suspendCoroutine { v ->
                    readWaitList.push(WaitEvent(v, data, offset, length))
                    selectionKey.listenReadable = true
                }
            }

            override suspend fun close() {
            }
        }

        val output: AsyncOutputStream = object : AsyncOutputStream {

            override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
                if (detached)
                    throw IllegalStateException("Connection was detached")
                return suspendCoroutine { v ->
                    writeWaitList.push(WaitEvent(v, data, offset, length))
                    selectionKey.listenWritable = true
                }
            }

            override suspend fun close() {
            }

            override suspend fun flush() {
            }
        }

        internal val readWaitList = Stack<WaitEvent>().asLiFoQueue()
        internal val writeWaitList = Stack<WaitEvent>().asLiFoQueue()

        operator fun invoke(func: suspend (Connection) -> Unit) {
            func.start(this)
        }

        internal var detached = false

        fun detach(): SocketChannel {
            detached = true
            if (!selectionKey.isCanlelled)
                selectionKey.cancel()
            while (!readWaitList.isEmpty) {
                readWaitList.pop().continuation.resumeWithException(RuntimeException("Connection Detached"))
            }
            while (!writeWaitList.isEmpty) {
                writeWaitList.pop().continuation.resumeWithException(RuntimeException("Connection Detached"))
            }
            return channel
        }

        override fun close() {
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
            val connection = Connection(channel = cl, attachment = null, manager = this)
            connection.channel.blocking = false
            connection.selectionKey = selector.reg(connection.channel, connection)
            val handler = it.attachment as ConnectHandler?
            handler?.clientConnected(connection, this)
        } else {
            val client = (it.attachment as Connection?)
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
                        println("ERROR #1: $e")
                        client.close()
                        throw e
                    }
                }
            }


            if (!client.selectionKey.isCanlelled && it.isWritable && client.selectionKey.attachment === client) {
                client.writeWaitList.pop(popResult)
                if (!popResult.isEmpty) {
                    val ev = popResult.value
                    try {
                        val wroteBytesCount = try {
                            client.channel.write(ev.data, ev.offset, ev.length)
                        } catch (e: Throwable) {
                            println("ERROR #2: $e")
                            ev.continuation.resumeWithException(e)
                            return@process
                        }
                        ev.continuation.resume(wroteBytesCount)
                    } catch (e: Throwable) {
                        println("ERROR #3: $e")
                        client.close()
                        throw e
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

    fun findClient(func: (Connection) -> Boolean): Connection? =
            selector.keys.asSequence().map { it.attachment as? Connection }.filterNotNull().find(func)

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

    fun connect(host: String, port: Int, attachment: Any? = null, factory: SocketFactory = SocketFactory.rawSocketFactory): Connection {
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
    fun attach(channel: SocketChannel, attachment: Any? = null): Connection {
        channel.blocking = false
        val connection = Connection(channel = channel, attachment = attachment, manager = this)
        connection.selectionKey = selector.reg(channel, connection)
        connection.selectionKey.listenReadable = false
        connection.selectionKey.listenWritable = false
        return connection
    }

    override fun close() {
        selector.keys.toTypedArray().forEach {
            val con = (it.attachment as? Connection) ?: return@forEach
            if (con.manager === this) {
                con.close()
            }
        }
        selector.close()
    }

    init {
        neverFreeze()
    }
}