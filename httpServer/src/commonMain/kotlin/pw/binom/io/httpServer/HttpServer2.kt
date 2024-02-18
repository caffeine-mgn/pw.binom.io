package pw.binom.io.httpServer

import kotlinx.coroutines.*
import pw.binom.ByteBufferPool
import pw.binom.atomic.AtomicBoolean
import pw.binom.collections.defaultMutableSet
import pw.binom.collections.removeAtUsingReplace
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.EOFException
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.HttpException
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.io.socket.MutableInetNetworkAddress
import pw.binom.io.use
import pw.binom.network.NetworkManager
import pw.binom.network.SocketClosedException
import pw.binom.pool.GenericObjectPool
import pw.binom.thread.DefaultUncaughtExceptionHandler
import pw.binom.thread.Thread
import pw.binom.thread.UncaughtExceptionHandler
import pw.binom.url.toURI
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class HttpServer2(
  val idleCheckInterval: Duration = 5.seconds,
  val handler: HttpHandler,
  val uncaughtExceptionHandler: UncaughtExceptionHandler = DefaultUncaughtExceptionHandler,
  val maxRequestLength: Int = 0,
  val maxHeaderLength: Int = 0,
  val readTimeout: Duration = Duration.INFINITE,
  val byteBufferPool: ByteBufferPool,
  val dispatcher: NetworkManager,
  val keepAlive: Boolean = true,
  val compressing: Boolean = true,
) : AsyncCloseable {
  private val closed = AtomicBoolean(false)
  private val listeners = defaultMutableSet<Job>()
  private val timeoutWaiters = ArrayList<TimeoutRecord>()
  private val timeoutRecordPool = GenericObjectPool(factory = TimeoutRecord)
  private val timeoutLock = SpinLock()
  private val scope =
    HttpServerScope(
      coroutineContext = dispatcher,
      server = this,
    )

  private suspend fun prepareTimeout(timeout: Duration): TimeoutRecord? {
    if (timeout.isInfinite()) {
      return null
    }
    val record = timeoutRecordPool.borrow()
    record.job = coroutineContext.job
    record.live = timeout.inWholeMilliseconds
    timeoutLock.synchronize { timeoutWaiters.add(record) }
    return record
  }

  private fun finishTimeout(record: TimeoutRecord?) {
    timeoutLock.synchronize {
      val index = timeoutWaiters.indexOf(record)
      if (index >= 0) {
        timeoutWaiters.removeAtUsingReplace(index)
      }
    }
  }

  @Suppress("OPT_IN_IS_NOT_ENABLED")
  @OptIn(ExperimentalContracts::class)
  private suspend inline fun <T> withTimeout(
    timeout: Duration,
    func: () -> T,
  ): T {
    contract {
      callsInPlace(func, InvocationKind.EXACTLY_ONCE)
    }
    val record = prepareTimeout(timeout)
    val result =
      try {
        func()
      } finally {
        finishTimeout(record)
      }
    return result
  }

  private suspend fun prepareExchange(stream: ServerAsyncAsciiChannel): HttpServerExchangeImpl {
    val reqMethod: String
    val reqPath: String
    val headers = HashHeaders2()
    withTimeout(readTimeout) {
      val line = stream.reader.readln() ?: throw EOFException()
      HttpServerUtils.parseHttpRequest(line) { method, path ->
        reqMethod = method
        reqPath = path
      }
      HttpServerUtils.readHeaders(headers, stream.reader)
    }
    return HttpServerExchangeImpl(
      requestURI = reqPath.toURI(),
      requestHeaders = headers,
      requestMethod = reqMethod,
      channel = stream,
      keepAliveEnabled = keepAlive,
      compressByteBufferPool = null,
      compressLevel = 0,
    )
  }

  private suspend fun clientProcessingWithMemoryLeaks(
    channel: AsyncChannel,
    address: InetNetworkAddress,
  ) {
    ServerAsyncAsciiChannel(
      pool = byteBufferPool,
      channel = channel,
      address = address,
    ).use { stream ->
      try {
        while (true) {
          val exchange =
            try {
              val b = prepareExchange(stream)
              b
            } catch (e: CancellationException) {
              break
            } catch (e: SocketClosedException) {
              break
            } catch (e: Throwable) {
              e.printStackTrace()
              uncaughtExceptionHandler.uncaughtException(
                thread = Thread.currentThread,
                throwable = e,
              )
              break
            }
          try {
            handler.handle(exchange)
          } catch (e: SocketClosedException) {
            e.printStackTrace()
            break
          } catch (e: Throwable) {
            if (!exchange.responseStarted) {
              uncaughtExceptionHandler.uncaughtException(
                thread = Thread.currentThread,
                throwable = e,
              )
              when (e) {
                is HttpException -> exchange.startResponse(e.code)
                else -> exchange.startResponse(500)
              }
            } else {
              uncaughtExceptionHandler.uncaughtException(
                thread = Thread.currentThread,
                throwable = e,
              )
              break
            }
          }
          if (!exchange.responseStarted) {
            try {
              exchange.startResponse(404)
            } catch (e: SocketClosedException) {
              break
            } catch (e: Throwable) {
              uncaughtExceptionHandler.uncaughtException(
                thread = Thread.currentThread,
                throwable = e,
              )
            }
          }
          exchange.finishRequest()
          if (!exchange.keepAlive) {
            break
          }
        }
      } finally {
        channel.asyncCloseAnyway()
      }
    }
  }

  private suspend fun clientProcessing(
    channel: AsyncChannel,
    address: InetNetworkAddress,
  ) {
    clientProcessingWithMemoryLeaks(channel, address)
//        clientProcessingNoMemoryLeaks(channel)
  }

  private val timeoutThread =
    Thread { _ ->
      val d = idleCheckInterval.inWholeMilliseconds
      while (!closed.getValue()) {
        Thread.sleep(d)
        timeoutLock.synchronize {
          var index = 0
          while (index < timeoutWaiters.size) {
            val e = timeoutWaiters[index]
            val s = e.live - d
            if (s < 0) {
              val job = e.job
              e.job = null
              timeoutRecordPool.recycle(e)
              timeoutWaiters.removeAtUsingReplace(index)
              job?.cancel()
            } else {
              e.live = s
              index++
            }
          }
        }
      }
    }.also { it.start() }

  private val incomeAddress = MutableInetNetworkAddress.create(host = "127.0.0.1", port = 8080)

  @Suppress("OPT_IN_IS_NOT_ENABLED")
  @OptIn(DelicateCoroutinesApi::class)
  fun listen(address: InetNetworkAddress): Job {
    val server = dispatcher.bindTcp(address)
    val job =
      GlobalScope.launch(dispatcher) {
        val currentJob = this.coroutineContext.job
        try {
          while (isActive) {
            val newClient =
              try {
                val client = server.accept(incomeAddress)
                client
              } catch (e: SocketClosedException) {
                break
              } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
              } catch (e: Throwable) {
                uncaughtExceptionHandler.uncaughtException(
                  thread = Thread.currentThread,
                  throwable = e,
                )
                continue
              }
            scope.launch { clientProcessing(channel = newClient, address = incomeAddress.clone()) }
          }
        } catch (e: kotlinx.coroutines.CancellationException) {
          // Do nothing
        } finally {
          server.closeAnyway()
          listeners -= currentJob
        }
      }
    listeners += job
    return job
  }

  override suspend fun asyncClose() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    val jobs = ArrayList(listeners)
    jobs.forEach {
      try {
        it.cancel()
      } catch (e: Throwable) {
        // Do nothing
      }
    }
    jobs.joinAll()
  }
}
