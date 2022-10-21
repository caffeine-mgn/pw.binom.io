package pw.binom.io.httpServer

import kotlinx.coroutines.*
import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.System
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableSet
import pw.binom.coroutines.onCancel
import pw.binom.date.DateTime
import pw.binom.io.AsyncCloseable
import pw.binom.io.ClosedException
import pw.binom.io.http.ReusableAsyncBufferedOutputAppendable
import pw.binom.io.http.ReusableAsyncChunkedOutput
import pw.binom.io.http.websocket.MessagePool
import pw.binom.io.http.websocket.WebSocketConnectionPool
import pw.binom.network.*
import pw.binom.pool.FixedSizePool
import pw.binom.thread.DefaultUncaughtExceptionHandler
import pw.binom.thread.Thread
import pw.binom.thread.UncaughtExceptionHandler
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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
    val maxIdleTime: Duration = 10.seconds,
    val idleCheckInterval: Duration = 30.seconds,
    internal val zlibBufferSize: Int = DEFAULT_BUFFER_SIZE,
    val errorHandler: UncaughtExceptionHandler = DefaultUncaughtExceptionHandler,
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

    private var lastIdleCheckTime = DateTime.nowTime
    private val binds = ArrayList<TcpServerConnection>()
    private val idleConnections = defaultMutableSet<ServerAsyncAsciiChannel>()

    internal fun browConnection(channel: ServerAsyncAsciiChannel) {
        idleConnections -= channel
    }

    suspend fun forceIdleCheck(): Int {
        var count = 0
        val now = DateTime.nowTime
        lastIdleCheckTime = now

        val it = idleConnections.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (now - e.lastActive > maxIdleTime.inWholeMilliseconds) {
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
        val now = DateTime.nowTime
        if (now - lastIdleCheckTime < idleCheckInterval.inWholeMilliseconds) {
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
                try {
                    handler.request(req)
                    idleCheck()
                } catch (e: Throwable) {
                    throw e
                }
            } catch (e: SocketClosedException) {
                runCatching { channel.asyncClose() }
            } catch (e: CancellationException) {
                runCatching { channel.asyncClose() }
            } catch (e: Throwable) {
                runCatching { channel.asyncClose() }
                runCatching { errorHandler.uncaughtException(Thread.currentThread, e) }
            } finally {
                if (req != null) {
                    req.free()
                    httpRequest2Impl.recycle(req)
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
                                val client = server.accept(null)
                                client
                            } catch (e: ClosedException) {
                                null
                            } ?: break
                            channel = ServerAsyncAsciiChannel(channel = client, pool = textBufferPool)
                            clientProcessing(channel = channel, isNewConnect = true)
                        } catch (e: Throwable) {
                            runCatching { channel?.asyncClose() }
                            break
                        }
                    }
                } finally {
                    binds -= server
                    runCatching { server.close() }
                }
            }
        }
        return listenJob.onCancel {
            closed.setValue(true)
            server.close()
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
        defaultMutableList(binds).forEach {
            runCatching { it.close() }
        }
        binds.clear()
    }
}
