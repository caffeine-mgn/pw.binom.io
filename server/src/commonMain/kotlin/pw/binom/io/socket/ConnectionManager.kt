package pw.binom.io.socket

import pw.binom.PopResult
import pw.binom.Stack
import pw.binom.io.AsyncInputStream
import pw.binom.io.AsyncOutputStream
import pw.binom.io.Closeable
import pw.binom.neverFreeze
import pw.binom.start
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class ConnectionManager : Closeable {

    internal class WaitEvent(val continuation: Continuation<Int>, val data: ByteArray, val offset: Int, val length: Int)

    inner class Connection internal constructor(internal val channel: SocketChannel, var attachment: Any?) : Closeable {
        internal lateinit var selectionKey: SocketSelector.SelectorKey
        val input: AsyncInputStream = object : AsyncInputStream {
            override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
                if (detached)
                    throw IllegalStateException("Connection was detached")
                return suspendCoroutine { v ->
                    readWaitList.push(WaitEvent(v, data, offset, length))
                    selectionKey.listenReadable = true
                }
            }

            override fun close() {
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

            override fun close() {
            }

            override suspend fun flush() {
            }
        }

        internal val readWaitList = Stack<WaitEvent>().asLiFoQueue()
        internal val writeWaitList = Stack<WaitEvent>().asLiFoQueue()

        operator fun invoke(func: suspend (Connection) -> Unit) {
            func.start(this)
        }

        private var detached = false

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
            detach().close()
        }

        init {
            neverFreeze()
        }
    }

    private val selector = SocketSelector(100)

    protected open fun connected(connection: Connection) {
        //
    }

    private val popResult = PopResult<WaitEvent>()

    fun update(timeout: Int? = null) = selector.process(timeout) {
        if (it.channel is ServerSocketChannel) {
            val server = it.channel as ServerSocketChannel
            val connection = Connection(channel = server.accept()!!, attachment = null)
            connection.channel.blocking = false
            connection.selectionKey = selector.reg(connection.channel, connection)
            connected(connection)
        } else {
            val client = it.attachment as Connection
            if (client.selectionKey.isCanlelled)
                return@process
            if (!client.selectionKey.isCanlelled && it.isReadable) {
                client.readWaitList.pop(popResult)
                if (!popResult.isEmpty) {
                    val ev = popResult.value
                    try {
                        val readBytesCount = try {
                            client.channel.read(ev.data, ev.offset, ev.length)
                        } catch (e: Throwable) {
                            ev.continuation.resumeWithException(e)
                            return@process
                        }
                        ev.continuation.resume(readBytesCount)
                    } catch (e: Throwable) {
                        client.close()
                        throw e
                    }
                }
            }

            if (!client.selectionKey.isCanlelled && it.isWritable) {
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
                        client.close()
                        throw e
                    }
                }
            }

            if (!client.selectionKey.isCanlelled) {
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

    fun bind(host: String = "0.0.0.0", port: Int) {
        val channel = ServerSocketChannel()
        channel.blocking = false
        channel.bind(host, port)
        selector.reg(channel)
    }

    fun connect(host: String, port: Int, attachment: Any? = null): Connection {
        val channel = SocketChannel()
        channel.connect(host, port)
        return attach(channel, attachment)
    }

    /**
     * Attach channel to current ConnectionManager
     */
    fun attach(channel: SocketChannel, attachment: Any? = null): Connection {
        channel.blocking = false
        val connection = Connection(channel = channel, attachment = attachment)
        connection.selectionKey = selector.reg(channel, connection)
        connection.selectionKey.listenReadable = false
        connection.selectionKey.listenWritable = false
        return connection
    }

    override fun close() {
        selector.keys.toTypedArray().forEach {
            it.channel.close()
        }
        selector.close()
    }

    init {
        neverFreeze()
    }
}