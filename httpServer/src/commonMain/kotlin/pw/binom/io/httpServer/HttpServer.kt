package pw.binom.io.httpServer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
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
//    val idleCheckInterval: Duration = 30.seconds,
    internal val zlibBufferSize: Int = DEFAULT_BUFFER_SIZE,
    val errorHandler: UncaughtExceptionHandler = DefaultUncaughtExceptionHandler,
    websocketMessagePoolSize: Int = 16,
    outputBufferPoolSize: Int = 16,
    chanckedAutoFlushBufferSize: Int = DEFAULT_BUFFER_SIZE,
    textBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : AsyncCloseable {
    internal val messagePool = MessagePool(initCapacity = 0)
    internal val webSocketConnectionPool = WebSocketConnectionPool(initCapacity = websocketMessagePoolSize)

    //    internal val textBufferPool = ByteBufferPool(capacity = 16)
    internal val textBufferPool =
        GenericObjectPool(initCapacity = 0, factory = ByteBufferFactory(size = textBufferSize))

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
        get() = idleJobsLock.synchronize { idleJobs.size }
    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Already closed")
        }
    }

    private val binds = ArrayList<TcpServerConnection>()
//    private val idleConnections = defaultMutableSet<ServerAsyncAsciiChannel>()
//    private var idleExchange = BatchExchange<ServerAsyncAsciiChannel?>()

    private val idleChannel = Channel<ServerAsyncAsciiChannel>(Channel.RENDEZVOUS)
    internal val idleJobs = defaultMutableSet<Job>()
    internal val idleJobsLock = SpinLock()

    internal fun browConnection(channel: ServerAsyncAsciiChannel) {
//        idleConnections -= channel
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
//        idleConnections += channel
        idleChannel.send(channel)
//        clientProcessing(channel = channel, isNewConnect = false)
    }

    private val idlePool = IdlePool { channel -> clientReProcessing(channel) }

    internal fun clientProcessing(
        channel: ServerAsyncAsciiChannel,
        isNewConnect: Boolean,
        timeout: Duration,
    ) = manager.launch {
        supervisorScope PROCESSING@{
            idleJobsLock.synchronize {
                idleJobs += coroutineContext.job
            }
            var req: HttpRequest3Impl? = null
            try {
                req = HttpRequest3Impl.read(
                    channel = channel,
                    server = this@HttpServer,
                    isNewConnect = isNewConnect,
                    readStartTimeout = timeout,
                    idleJob = this.coroutineContext.job,
                    returnToIdle = idlePool,
                ).getOrThrow()

//                req = HttpRequest2Impl.read(
//                    channel = channel,
//                    server = this@HttpServer,
//                    isNewConnect = isNewConnect,
//                )
                if (req == null) {
//                println("HttpServer:: reading timeout!")
                    channel.asyncCloseAnyway()
                    return@PROCESSING
                }
//            println("HttpServer:: request got! Processing...")
                handler.request(req)
                if (req.response == null) {
                    req.response { it.status = 404 }
                }
//                idleCheck()
            } catch (e: TimeoutCancellationException) {
//            println("HttpServer:: reading timeout!")
                req = null
                channel.asyncCloseAnyway()
            } catch (e: CancellationException) {
//            println("HttpServer:: reading cancelled!")
                req = null
                channel.asyncCloseAnyway()
            } catch (e: SocketClosedException) {
                req = null
                channel.asyncCloseAnyway()
            } catch (e: Throwable) {
                req = null
                channel.asyncCloseAnyway()
                try {
                    errorHandler.uncaughtException(Thread.currentThread, e)
                } catch (e: Throwable) {
                    // Do nothing
                }
            } finally {
//                if (req != null) {
//                    req.free()
//                    httpRequest2Impl.recycle(req)
//                }
            }
        }
    }

    private val idleProcessing = manager.launch {
        while (isActive && !closed) {
            val networkChannel = try {
                idleChannel.receive()
            } catch (e: CancellationException) {
                break
            }
            manager.launch {
                clientProcessing(
                    channel = networkChannel,
                    isNewConnect = false,
                    timeout = maxIdleTime,
                )
//                idleJobsLock.synchronize {
//                    idleJobs += thisJob
//                }
            }
        }
    }

    fun listenHttp(address: NetworkAddress, networkManager: NetworkManager = Dispatchers.Network): Job {
        val serverChannel = Socket.createTcpServerNetSocket()
        serverChannel.bind(address)
        serverChannel.blocking = false
        val server = networkManager.attach(serverChannel)
        server.description = address.toString()
        binds += server

        val closed = AtomicBoolean(false)
        val listenJob = manager.launch(networkManager)/*(start = CoroutineStart.UNDISPATCHED)*/ {
//            withContext(dispatcher) {
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
                        }
                        if (client == null) {
                            break
                        }
                        channel = ServerAsyncAsciiChannel(
                            channel = client,
                            pool = textBufferPool,
                        )
                        clientProcessing(
                            channel = channel,
                            isNewConnect = true,
                            timeout = maxIdleTime,
                        )
                    } catch (e: Throwable) {
                        this@HttpServer.errorHandler.uncaughtException(Thread.currentThread, e)
                        channel?.asyncCloseAnyway()
                        break
                    }
                }
            } finally {
                binds -= server
                server.closeAnyway()
            }
//            }
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
        idleJobsLock.synchronize {
            idleJobs.forEach {
                it.cancel()
            }
            idleJobs.clear()
        }
//        idleConnections.forEach {
//            it.asyncCloseAnyway()
//        }
//        idleConnections.clear()
        defaultMutableList(binds).forEach {
            it.closeAnyway()
        }
        binds.clear()
    }
}
