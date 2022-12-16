package pw.binom.io.httpServer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableSet
import pw.binom.coroutines.onCancel
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBufferFactory
import pw.binom.io.ClosedException
import pw.binom.io.http.ReusableAsyncBufferedOutputAppendable
import pw.binom.io.http.ReusableAsyncChunkedOutput
import pw.binom.io.http.websocket.MessagePool
import pw.binom.io.http.websocket.WebSocketConnectionPool
import pw.binom.io.socket.NetworkAddress
import pw.binom.io.socket.Socket
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.network.SocketClosedException
import pw.binom.network.TcpServerConnection
import pw.binom.pool.GenericObjectPool
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
    chanckedAutoFlushBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : AsyncCloseable {
    internal val messagePool = MessagePool(initCapacity = 0)
    internal val webSocketConnectionPool = WebSocketConnectionPool(initCapacity = websocketMessagePoolSize)
    internal val textBufferPool = ByteBufferPool(capacity = 16)
    internal val httpRequest2Impl = GenericObjectPool(initCapacity = 0, factory = HttpRequest2Impl.Manager)
    internal val httpResponse2Impl = GenericObjectPool(initCapacity = 0, factory = HttpResponse2Impl.Manager)
    internal val reusableAsyncChunkedOutputPool = GenericObjectPool(
        factory = ReusableAsyncChunkedOutput.Factory(autoFlushBuffer = chanckedAutoFlushBufferSize),
        initCapacity = 0,
    )
    internal val compressBufferPool = GenericObjectPool(initCapacity = 0, factory = ByteBufferFactory(zlibBufferSize))
    internal val bufferWriterPool = GenericObjectPool(
        factory = ReusableAsyncBufferedOutputAppendable.Manager(),
        initCapacity = 0,
    )
    val idleConnectionSize: Int
        get() = idleConnections.size
    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Already closed")
        }
    }

    private val binds = ArrayList<TcpServerConnection>()
    private val idleConnections = defaultMutableSet<ServerAsyncAsciiChannel>()
//    private var idleExchange = BatchExchange<ServerAsyncAsciiChannel?>()

    private val idleChannel = Channel<ServerAsyncAsciiChannel>(Channel.RENDEZVOUS)

    internal fun browConnection(channel: ServerAsyncAsciiChannel) {
        idleConnections -= channel
        HttpServerMetrics.idleHttpServerConnection.dec()
    }

//    suspend fun forceIdleCheck(): Int {
//        var count = 0
//        val now = DateTime.nowTime
//        lastIdleCheckTime = now
//
//        val it = idleConnections.iterator()
//        while (it.hasNext()) {
//            val e = it.next()
//            if (now - e.lastActive > maxIdleTime.inWholeMilliseconds) {
//                count++
//                it.remove()
//                runCatching { e.asyncClose() }
//                HttpServerMetrics.idleHttpServerConnection.dec()
//            }
//        }
//        if (count > 0) {
//            System.gc()
//        }
//        return count
//    }

//    private suspend fun idleCheck() {
//        val now = DateTime.nowTime
//        if (now - lastIdleCheckTime < idleCheckInterval.inWholeMilliseconds) {
//            return
//        }
//        forceIdleCheck()
//    }

    internal suspend fun clientReProcessing(channel: ServerAsyncAsciiChannel) {
        channel.activeUpdate()
        HttpServerMetrics.idleHttpServerConnection.inc()
        idleConnections += channel
        idleChannel.send(channel)
//        clientProcessing(channel = channel, isNewConnect = false)
    }

    internal fun clientProcessing(channel: ServerAsyncAsciiChannel, isNewConnect: Boolean) {
        GlobalScope.launch(manager) {
            var req: HttpRequest2Impl? = null
            try {
                req = HttpRequest2Impl.read(
                    channel = channel,
                    server = this@HttpServer,
                    isNewConnect = isNewConnect,
                    readStartTimeout = maxIdleTime
                )

//                req = HttpRequest2Impl.read(
//                    channel = channel,
//                    server = this@HttpServer,
//                    isNewConnect = isNewConnect,
//                )
                if (req == null) {
                    runCatching { channel.asyncClose() }
                    return@launch
                }
                handler.request(req)
//                idleCheck()
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

    private val idleProcessing = GlobalScope.launch(manager) {
        while (isActive && !closed) {
            val networkChannel = try {
                idleChannel.receive()
            } catch (e: CancellationException) {
                break
            }
            try {
                clientProcessing(channel = networkChannel, isNewConnect = false)
            } catch (e: Throwable) {
                runCatching { networkChannel.asyncClose() }
            }
        }
    }

    fun listenHttp(address: NetworkAddress, dispatcher: NetworkManager = Dispatchers.Network): Job {
        val serverChannel = Socket.createTcpServerNetSocket()
        serverChannel.bind(address)
        serverChannel.blocking = false
        val server = dispatcher.attach(serverChannel)
        server.description = address.toString()
        binds += server

        val closed = AtomicBoolean(false)
        val listenJob = GlobalScope.launch(dispatcher, start = CoroutineStart.UNDISPATCHED) {
            withContext(dispatcher) {
                try {
                    while (!closed.getValue()) {
                        var channel: ServerAsyncAsciiChannel? = null
                        try {
//                            idleCheck()
                            val client = try {
                                val client = server.accept(null)
                                client
                            } catch (e: ClosedException) {
                                null
                            } catch (e: SocketClosedException) {
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
        idleProcessing.cancelAndJoin()
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
