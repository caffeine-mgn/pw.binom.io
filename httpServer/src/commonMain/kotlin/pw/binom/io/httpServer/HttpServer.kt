package pw.binom.io.httpServer

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.Closeable
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.SocketClosedException
import pw.binom.network.TcpServerConnection

interface Handler {
    suspend fun request(req: HttpRequest2)
}

fun Handler(func: suspend (HttpRequest2) -> Unit) = object : Handler {
    override suspend fun request(req: HttpRequest2) {
        func(req)
    }

}

/**
 * Base Http Server
 *
 * @param handler request handler
 * @param zlibBufferSize size of zlib buffer. 0 - disable zlib
 * @param errorHandler handler for error during request processing
 */
class HttpServer(
    val manager: NetworkDispatcher,
    val handler: Handler,
    internal val zlibBufferSize: Int = DEFAULT_BUFFER_SIZE,
    val errorHandler: (Throwable) -> Unit = { e ->
        RuntimeException(
            "Exception during http processing",
            e
        ).printStackTrace()
    }
) : Closeable {
    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Already closed")
        }
    }

    private val binds = ArrayList<TcpServerConnection>()

    internal fun clientProcessing(channel: AsyncAsciiChannel) {
        manager.async {
            try {
                val req = HttpRequest2Impl.read(channel = channel, server = this)
                handler.request(req)
            } catch (e: SocketClosedException) {
                //IGNORE
            } catch (e: Throwable) {
                runCatching { channel.asyncClose() }
                runCatching { errorHandler(e) }
            }
        }
    }

    fun bind(address: NetworkAddress): Closeable {
        val server = manager.bindTcp(address)
        binds += server
        manager.async {
            while (!closed) {
                try {
                    val client = server.accept(null) ?: continue
                    val channel = AsyncAsciiChannel(client)
                    clientProcessing(channel)
                } catch (e: SocketClosedException) {
                    //IGNORE
                }
            }
        }
        return Closeable {
            checkClosed()
            binds -= server
            server.close()
        }
    }

    override fun close() {
        checkClosed()
        closed = true
        binds.forEach {
            runCatching { it.close() }
        }
        binds.clear()
    }
}