package pw.binom.io.socket

import pw.binom.io.AsyncInputStream
import pw.binom.io.Closeable
import pw.binom.io.OutputStream
import kotlin.coroutines.*

//fun <T> (suspend () -> T).start() {
//    this.startCoroutine(object : Continuation<T> {
//        override val context: CoroutineContext = EmptyCoroutineContext
//
//        override fun resumeWith(result: Result<T>) {
//            result.getOrThrow()
//        }
//
//    })
//}

private fun <P, T> (suspend (P) -> T).start(value: P) {
    this.startCoroutine(value, object : Continuation<T> {
        override val context: CoroutineContext = EmptyCoroutineContext

        override fun resumeWith(result: Result<T>) {
            result.getOrThrow()
        }

    })
}

private open class EmptyContinuation(override val context: CoroutineContext = EmptyCoroutineContext) : Continuation<Any?> {
    companion object : EmptyContinuation()

    override fun resumeWith(result: Result<Any?>) {
        result.getOrThrow()
    }
}

abstract class AsyncServer(val port: Int) : Closeable {

    private val server = ServerSocketChannel().also {
        it.blocking = false
    }

    private class WaitEvent(val continuation: Continuation<Int>, val data: ByteArray, val offset: Int, val length: Int)

    private inner class Client(val channel: SocketChannel) : AsyncInputStream {

        val waitList = ArrayList<WaitEvent>()

        override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
            return suspendCoroutine { v ->
                waitList.add(WaitEvent(v, data, offset, length))
            }
        }

        override fun close() {
            channel.close()
        }

        suspend fun run() {
            try {
                client(this, channel)
            } finally {
                channel.close()
            }
        }
    }

    private val selector = SocketSelector(100)

    abstract suspend fun client(input: AsyncInputStream, output: OutputStream)

    private val starter: suspend (Client) -> Unit = {
        it.run()
    }

    fun start() {
        server.bind(port)
        selector.reg(server)


        while (true) {
            selector.process {
                if (it.channel === server) {
                    val client = Client(server.accept()!!)
                    client.channel.blocking = false
                    selector.reg(client.channel, client)
                    starter.start(client)
                } else {
                    try {
                        val client = it.attachment as Client
                        val ev = client.waitList.lastOrNull() ?: return@process
                        client.waitList.removeAt(client.waitList.lastIndex)
                        ev.continuation.resume(client.channel.read(ev.data, ev.offset, ev.length))
                    } catch (e: Throwable) {
                        it.cancel()
                        throw e
                    }
                }
            }
        }
    }

    override fun close() {
        server.close()
        selector.close()
    }
}