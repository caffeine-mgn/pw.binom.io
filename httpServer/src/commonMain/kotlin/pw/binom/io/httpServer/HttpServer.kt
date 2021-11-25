package pw.binom.io.httpServer

import kotlinx.coroutines.*
import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.System
import pw.binom.date.Date
import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable
import pw.binom.network.*

interface Handler {
    suspend fun request(req: HttpRequest)
}

fun Handler(func: suspend (HttpRequest) -> Unit) = object : Handler {
    override suspend fun request(req: HttpRequest) {
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
    val maxIdleTime: Long = 10_000,
    val idleCheckInterval: Long = 30_000,
    internal val zlibBufferSize: Int = DEFAULT_BUFFER_SIZE,
    val errorHandler: (Throwable) -> Unit = { e ->
        RuntimeException(
            "Exception during http processing",
            e
        ).printStackTrace()
    }
) : AsyncCloseable {
    internal val textBufferPool = ByteBufferPool(capacity = 16)
    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Already closed")
        }
    }

    private val binds = ArrayList<TcpServerConnection>()

    private val idleConnections = HashSet<ServerAsyncAsciiChannel>()

    internal fun browConnection(channel: ServerAsyncAsciiChannel) {
        idleConnections -= channel
    }

    private var lastIdleCheckTime = Date.nowTime

    suspend fun forceIdleCheck(): Int {
        var count = 0
        val now = Date.nowTime
        lastIdleCheckTime = now

        val it = idleConnections.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (now - e.lastActive > maxIdleTime) {
                count++
                it.remove()
                runCatching { e.asyncClose() }
            }
        }
        if (count > 0) {
            System.gc()
        }
        return count
    }

    private suspend fun idleCheck() {
        val now = Date.nowTime
        if (now - lastIdleCheckTime < idleCheckInterval) {
            return
        }
        forceIdleCheck()
    }

    val idleConnectionSize: Int
        get() = idleConnections.size

    internal fun clientReProcessing(channel: ServerAsyncAsciiChannel) {
        channel.activeUpdate()
        idleConnections += channel
        clientProcessing(channel = channel, isNewConnect = false)
    }

    internal fun clientProcessing(channel: ServerAsyncAsciiChannel, isNewConnect: Boolean) {
        manager.startCoroutine {
            try {
                val req = HttpRequest2Impl.read(channel = channel, server = this, isNewConnect = isNewConnect)
                handler.request(req)
                idleCheck()
            } catch (e: SocketClosedException) {
                runCatching { channel.asyncClose() }
            } catch (e: Throwable) {
                runCatching { channel.asyncClose() }
                runCatching { errorHandler(e) }
            }
        }
    }

    fun listenHttp(nd:NetworkCoroutineDispatcher=Dispatchers.Network, address: NetworkAddress): Job {
        val serverChannel = TcpServerSocketChannel()
        serverChannel.bind(address)
        val server = nd.attach(serverChannel)
        binds += server
        nd as CoroutineScope
        return nd.async {
            try {
                val currentJob = this.coroutineContext[Job]!!
                while (!currentJob.isCancelled) {
                    var channel: ServerAsyncAsciiChannel? = null
                    try {
                        idleCheck()
                        val client = server.accept(null) ?: continue
                        channel = ServerAsyncAsciiChannel(channel = client, pool = textBufferPool)
                        clientProcessing(channel = channel, isNewConnect = true)
                    } catch (e: Throwable) {
                        runCatching { channel?.asyncClose() }
                    }
                }
            } finally {
                binds -= server
                server.close()
            }
        }
//        return Closeable {
//            checkClosed()
//            binds -= server
//            server.close()
//        }
    }

    fun bindHttp(address: NetworkAddress): Closeable {
        val server = manager.bindTcp(address)
        binds += server
        manager.startCoroutine {
            while (!closed) {
                var channel: ServerAsyncAsciiChannel? = null
                try {
                    idleCheck()
                    val client = server.accept(null) ?: continue
                    channel = ServerAsyncAsciiChannel(channel = client, pool = textBufferPool)
                    clientProcessing(channel = channel, isNewConnect = true)
                } catch (e: Throwable) {
                    runCatching { channel?.asyncClose() }
                }
            }
        }
        return Closeable {
            checkClosed()
            binds -= server
            server.close()
        }
    }

    override suspend fun asyncClose() {
        checkClosed()
        closed = true
        binds.forEach {
            runCatching { it.close() }
        }
        binds.clear()
        idleConnections.forEach {
            runCatching { it.asyncClose() }
        }
        idleConnections.clear()
    }
}