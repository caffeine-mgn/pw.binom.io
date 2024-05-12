package pw.binom.io.httpServer

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.coroutines.onCancel
import pw.binom.date.DateTime
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBufferFactory
import pw.binom.io.ClosedException
import pw.binom.io.http.ReusableAsyncBufferedOutputAppendable
import pw.binom.io.http.ReusableAsyncChunkedOutput
import pw.binom.io.socket.*
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.network.SocketClosedException
import pw.binom.network.TcpServerConnection
import pw.binom.pool.GenericObjectPool
import pw.binom.thread.DefaultUncaughtExceptionHandler
import pw.binom.thread.Thread
import pw.binom.thread.UncaughtExceptionHandler
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

/**
 * Base Http Server
 *
 * @param handler request handler
 * @param zlibBufferSize size of zlib buffer. 0 - disable zlib
 * @param errorHandler handler for error during request processing
 */
@Deprecated(message = "Use HttpServer2")
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
  timeoutCheckInterval: Duration = 30.seconds,
) : AsyncCloseable {
  init {
    require((timeoutCheckInterval.isPositive() || timeoutCheckInterval == ZERO) && timeoutCheckInterval.isFinite())
  }

//  internal val messagePool = MessagePool(initCapacity = 0)
//    internal val webSocketConnectionPool = WebSocketConnectionPool(initCapacity = websocketMessagePoolSize)

  private val timeoutThreads = defaultMutableMap<Job, Long>()
  private val timeoutThreadsLock = SpinLock()

  internal suspend fun waitTimeout(time: Duration) {
    val currentJob = coroutineContext[Job] ?: return
    timeoutThreadsLock.synchronize {
      timeoutThreads[currentJob] = DateTime.nowTime + time.inWholeMilliseconds
    }
  }

  internal suspend fun timeoutFinished() {
    val currentJob = coroutineContext[Job] ?: return
    timeoutThreadsLock.synchronize {
      timeoutThreads.remove(currentJob)
    }
  }

  private val timeoutThread: Thread? = timeoutCheckInterval
    .takeIf { it.isPositive() }
    ?.let { timeCheck ->
      Thread { self ->
        while (!closed.getValue()) {
          Thread.sleep(timeCheck)
          val now = DateTime.nowTime
          timeoutThreadsLock.synchronize {
            val it = timeoutThreads.iterator()
            while (it.hasNext()) {
              val e = it.next()
              if (e.value < now) {
                e.key.cancel(CancellationException(null, HttpReadTimeoutException()))
                it.remove()
              }
            }
          }
        }
      }
    }

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
  private var closed = AtomicBoolean(false)
  private fun checkClosed() {
    if (closed.getValue()) {
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
//                    isNewConnect = isNewConnect,
          readStartTimeout = timeout,
//                    idleJob = this.coroutineContext.job,
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
    while (isActive && !closed.getValue()) {
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

  fun listenHttp(address: InetSocketAddress, networkManager: NetworkManager = Dispatchers.Network): Job {
    val serverChannel = TcpNetServerSocket()
    serverChannel.bind(address)
    serverChannel.blocking = false
    val server = networkManager.attach(serverChannel)
    server.description = address.toString()
    binds += server
    val address = MutableInetAddress()

    val closed = AtomicBoolean(false)
    val listenJob = GlobalScope.launch(networkManager)/*(start = CoroutineStart.UNDISPATCHED)*/ {
//            withContext(dispatcher) {
      try {
        while (!closed.getValue()) {
          var channel: ServerAsyncAsciiChannel? = null
          try {
//                            idleCheck()
            val client = try {
              val client = server.accept(address)
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
              address = address.toImmutable(),
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
    if (!closed.compareAndSet(false, true)) {
      return
    }
    timeoutThread?.join()
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
