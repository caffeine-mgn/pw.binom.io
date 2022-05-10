package pw.binom.io.httpServer

import kotlinx.coroutines.*
import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.System
import pw.binom.atomic.AtomicBoolean
import pw.binom.date.Date
import pw.binom.io.AsyncCloseable
import pw.binom.io.ClosedException
import pw.binom.io.http.ReusableAsyncBufferedOutputAppendable
import pw.binom.io.http.ReusableAsyncChunkedOutput
import pw.binom.io.http.websocket.MessagePool
import pw.binom.io.http.websocket.WebSocketConnectionPool
import pw.binom.network.*
import pw.binom.pool.FixedSizePool

fun interface Handler {
    suspend fun request(req: HttpRequest)
}

/**
 * Base Http Server
 *
 * @param handler request handler
 * @param zlibBufferSize size of zlib buffer. 0 - disable zlib
 * @param errorHandler handler for error during request processing
 */
class HttpServer(
    val manager: NetworkManager = Dispatchers.Network,
    val handler: Handler,
    val maxIdleTime: Long = 10_000,
    val idleCheckInterval: Long = 30_000,
    internal val zlibBufferSize: Int = DEFAULT_BUFFER_SIZE,
    val errorHandler: (Throwable) -> Unit = { e ->
        e.printStackTrace()
    },
    websocketMessagePoolSize: Int = 16,
    outputBufferPoolSize: Int = 16,
) : AsyncCloseable {
    internal val messagePool by lazy { MessagePool(websocketMessagePoolSize) }
    internal val webSocketConnectionPool by lazy { WebSocketConnectionPool(websocketMessagePoolSize) }
    internal val textBufferPool = ByteBufferPool(capacity = 16)
    internal val httpRequest2Impl = FixedSizePool(16, HttpRequest2Impl.Manager)
    internal val httpResponse2Impl = FixedSizePool(16, HttpResponse2Impl.Manager)
    internal val reusableAsyncChunkedOutputPool by lazy { ReusableAsyncChunkedOutput.Pool(outputBufferPoolSize) }
    internal val bufferWriterPool by lazy {
        FixedSizePool(
            outputBufferPoolSize,
            ReusableAsyncBufferedOutputAppendable.Manager()
        )
    }
    val idleConnectionSize: Int
        get() = idleConnections.size
    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Already closed")
        }
    }

    private var lastIdleCheckTime = Date.nowTime
    private val binds = ArrayList<TcpServerConnection>()
    private val idleConnections = HashSet<ServerAsyncAsciiChannel>()

    internal fun browConnection(channel: ServerAsyncAsciiChannel) {
        idleConnections -= channel
    }

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

    internal fun clientReProcessing(channel: ServerAsyncAsciiChannel) {
        channel.activeUpdate()
        idleConnections += channel
        clientProcessing(channel = channel, isNewConnect = false)
    }

    internal fun clientProcessing(channel: ServerAsyncAsciiChannel, isNewConnect: Boolean) {
        GlobalScope.launch(manager) {
            var req: HttpRequest2Impl? = null
            try {
                req = HttpRequest2Impl.read(
                    channel = channel,
                    server = this@HttpServer,
                    isNewConnect = isNewConnect,
                )
                handler.request(req)
                idleCheck()
            } catch (e: SocketClosedException) {
                runCatching { channel.asyncClose() }
            } catch (e: CancellationException) {
                runCatching { channel.asyncClose() }
            } catch (e: Throwable) {
                runCatching { channel.asyncClose() }
                runCatching { errorHandler(e) }
            } finally {
                if (req != null && !req.isFree) {
                    req.free()
                }
            }
        }
    }

    fun listenHttp(address: NetworkAddress, dispatcher: NetworkManager = Dispatchers.Network): Job {
        val serverChannel = TcpServerSocketChannel()
        serverChannel.bind(address)
        serverChannel.setBlocking(false)
        val server = dispatcher.attach(serverChannel)
        binds += server

        val closed = AtomicBoolean(false)
        val listenJob = GlobalScope.launch(dispatcher, start = CoroutineStart.UNDISPATCHED) {
            withContext(dispatcher) {
                try {
                    while (!closed.getValue()) {
                        var channel: ServerAsyncAsciiChannel? = null
                        try {
                            idleCheck()
                            val client = try {
                                server.accept(null)
                            } catch (e: ClosedException) {
                                null
                            } ?: break
                            channel = ServerAsyncAsciiChannel(channel = client, pool = textBufferPool)
                            clientProcessing(channel = channel, isNewConnect = true)
                        } catch (e: Throwable) {
                            runCatching { channel?.asyncClose() }
                        }
                    }
                } finally {
                    println("Finish!")
                    binds -= server
                    runCatching { server.close() }
                }
            }
        }
        return JobWithCancelWaiter(listenJob) {
            closed.setValue(true)
//            GlobalScope.launch(dispatcher) {
            server.close()
//            }
        }
    }

    override suspend fun asyncClose() {
        checkClosed()
        closed = true
        textBufferPool.close()
        httpRequest2Impl.close()
        httpResponse2Impl.close()
        reusableAsyncChunkedOutputPool.close()
        bufferWriterPool.close()
        idleConnections.forEach {
            runCatching { it.asyncClose() }
        }
        idleConnections.clear()
        binds.forEach {
            runCatching { it.close() }
        }
        binds.clear()
    }
}

private class JobWithCancelWaiter(val job: Job, val func: (cause: CancellationException?) -> Unit) : Job by job {
    override fun cancel(cause: CancellationException?) {
        func(cause)
        job.cancel(cause)
    }
}
