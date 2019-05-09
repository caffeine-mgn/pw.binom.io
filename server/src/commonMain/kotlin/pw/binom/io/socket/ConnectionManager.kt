package pw.binom.io.socket

import pw.binom.Stack
import pw.binom.io.AsyncInputStream
import pw.binom.io.Closeable
import pw.binom.io.OutputStream
import pw.binom.neverFreeze
import kotlin.coroutines.*

private fun <P, T> (suspend (P) -> T).start(value: P) {
    this.startCoroutine(value, object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            result.getOrThrow()
        }
    })
}

open class ConnectionManager : Closeable {

    internal class WaitEvent(val continuation: Continuation<Int>, val data: ByteArray, val offset: Int, val length: Int)

    inner class Connection internal constructor(internal val channel: SocketChannel, var attachment: Any?) : Closeable {
        internal lateinit var selectionKey: SocketSelector.SelectorKey
        val input: AsyncInputStream = object : AsyncInputStream {
            override suspend fun read(data: ByteArray, offset: Int, length: Int): Int =
                    suspendCoroutine { v ->
                        waitList.push(WaitEvent(v, data, offset, length))
                    }

            override fun close() {

            }
        }

        val output: OutputStream = channel

        internal val waitList = Stack<WaitEvent>().asLiFoQueue()

        operator fun invoke(func: suspend (Connection) -> Unit) {
            func.start(this)
        }

        fun detach(): SocketChannel {
            selectionKey.cancel()
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

    fun update(timeout: Int? = null) {
        selector.process(timeout) {
            if (it.channel is ServerSocketChannel) {
                val server = it.channel as ServerSocketChannel
                val connection = Connection(channel = server.accept()!!, attachment = null)
                connection.channel.blocking = false
                connection.selectionKey = selector.reg(connection.channel, connection)
                connected(connection)
            } else {
                val client = it.attachment as Connection
                if (client.waitList.isEmpty)
                    return@process
                val ev = client.waitList.pop()
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