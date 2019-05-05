package pw.binom.io.socket

import pw.binom.Stack
import pw.binom.io.AsyncInputStream
import pw.binom.io.Closeable
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

    inner class Connection(var attachment: Any?, internal val channel: SocketChannel) : Closeable {
        val input = object : AsyncInputStream {
            override suspend fun read(data: ByteArray, offset: Int, length: Int): Int =
                    suspendCoroutine { v ->
                        waitList.push(WaitEvent(v, data, offset, length))
                    }

            override fun close() {

            }
        }

        val output = channel

        internal val waitList = Stack<WaitEvent>().asLiFoQueue()

        override fun close() {
            channel.close()
        }

        operator fun invoke(func: suspend (Connection) -> Unit) {
            func.start(this)
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
                val connection = Connection(null, server.accept()!!)
                connection.channel.blocking = false
                selector.reg(connection.channel, connection)
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
                    it.cancel()
                    it.channel.close()
                    throw e
                }
            }
        }
    }

    fun findClient(func: (Connection) -> Boolean): Connection? =
            selector.keys.asSequence().map { it.attachment as? Connection }.filterNotNull().find(func)

    val clientSize: Int
        get() = selector.keys.size

    fun bind(port: Int) {
        val channel = ServerSocketChannel()
        channel.blocking = false
        channel.bind(port)
        selector.reg(channel)
    }

    fun connect(host: String, port: Int, attachment: Any? = null): Connection {
        val channel = SocketChannel()
        channel.connect(host, port)
        channel.blocking = false
        val connection = Connection(attachment, channel)
        selector.reg(channel, connection)
        return connection
    }

    override fun close() {
        selector.keys.toTypedArray().forEach {
            it.channel.close()
        }
        selector.close()
    }
}