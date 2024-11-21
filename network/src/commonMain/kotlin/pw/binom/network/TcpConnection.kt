@file:OptIn(ExperimentalCoroutinesApi::class)

package pw.binom.network

import kotlinx.coroutines.*
import pw.binom.InternalLog
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.executeAndResumeWithException
import pw.binom.io.AsyncChannel
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException
import pw.binom.io.DataTransferSize
import pw.binom.io.socket.ListenFlags
import pw.binom.io.socket.SelectorKey
import pw.binom.io.socket.TcpClientSocket
import pw.binom.io.socket.addListen
import pw.binom.resumeOnException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.time.TimeSource.Monotonic.ValueTimeMark
import kotlin.time.measureTime

class TcpConnection(
  val channel: TcpClientSocket,
  private val currentKey: SelectorKey,
) : AbstractConnection(), AsyncChannel {
  private var connect: CancellableContinuation<Unit>? = null
  var description: String? = null

  private val logger = InternalLog.file("TcpConnection").prefix { "$currentKey " }

  override fun toString(): String = "TcpConnection($description)"

  private class IOState {
    var continuation: CancellableContinuation<DataTransferSize>? = null
      private set
    var suspended: Throwable? = null
      private set
    var id: Int? = null
      private set
    var start: ValueTimeMark? = null
      private set
    var data: ByteBuffer? = null
      private set
    var full = false

    fun reset() {
      continuation = null
      data = null
      id = null
    }

    fun resume(result: Result<DataTransferSize>) {
      val c = continuation
      reset()
      c!!.resumeWith(result)
    }

    fun set(
      continuation: CancellableContinuation<DataTransferSize>,
      data: ByteBuffer,
      id: Int,
      start: ValueTimeMark,
    ) {
      suspended = Throwable()
      this.id = id
      this.continuation = continuation
      this.data = data
      this.start = start
    }

    fun cancel(throwable: Throwable? = null) {
      val continuation = continuation
      continuation?.cancel(throwable)
      this.continuation = null
      data = null
    }

    fun exception(e: Throwable) {
      val continuation = continuation
      this.continuation = null
      continuation?.resumeWithException(e)
    }
  }

  private val readData = IOState()
  private val sendData = IOState()
  private val readLock = SpinLock()

  private fun calcListenFlags() = when {
    readData.continuation != null && (sendData.continuation != null) -> ListenFlags.READ + ListenFlags.ERROR + ListenFlags.WRITE
    readData.continuation != null -> ListenFlags.READ + ListenFlags.ERROR
    sendData.continuation != null -> ListenFlags.WRITE + ListenFlags.ERROR
    else -> ListenFlags.ZERO
  }

  override fun readyForWrite(key: SelectorKey) {
    if (currentKey.watching) {
      println("TcpConnection::readyForWrite #1 id=${sendData.id}, time=${sendData.start?.elapsedNow()}")
    }
    if (checkConnect()) {
      if (currentKey.watching) {
        println("TcpConnection::readyForWrite #2 id=${sendData.id}, time=${sendData.start?.elapsedNow()}")
      }
      return
    }
    val con = sendData.continuation
    if (con != null) {
      if (currentKey.watching) {
        println("TcpConnection::readyForWrite #3 id=${sendData.id}, time=${sendData.start?.elapsedNow()}")
      }
      val result = runCatching { DataTransferSize.ofSize(channel.send(sendData.data!!)) }
      if (currentKey.watching) {
        println("TcpConnection::readyForWrite #3.1 result=${result.getOrNull()}, id=${sendData.id}, time=${sendData.start?.elapsedNow()}")
      }
      if (result.isSuccess && result.getOrThrow().isNotAvailable) {
        if (currentKey.watching) {
          println("TcpConnection::readyForWrite $currentKey #4 selector, id=${sendData.id}, time=${sendData.start?.elapsedNow()}")
        }
        sendData.exception(SocketClosedException())
        return
      }
//      if (sendData.data!!.remaining == 0) {

//                key.removeListen(KeyListenFlags.WRITE)
      if (currentKey.watching) {
        println("TcpConnection::readyForWrite #5 Resume id=${sendData.id}, , currentKey=$currentKey, time=${sendData.start?.elapsedNow()}, result: $result SUSPENDED ON:\n${sendData.suspended?.stackTraceToString()}")
      }
      sendData.resume(result)
//      if (currentKey.watching) {
//        println("TcpConnection::readyForWrite $currentKey #5.2, id=${sendData.id}, time=${sendData.start?.elapsedNow()}")
//      }
//      } else {
//        key.addListen(ListenFlags.WRITE + ListenFlags.ERROR + ListenFlags.ONCE)
//      }
//      if (sendData.continuation == null) {
//        if (!currentKey.updateListenFlags(calcListenFlags())) {
//          closeAnyway()
//        }
//      }
    } else {
      if (currentKey.watching) {
        println("TcpConnection::readyForWrite , currentKey=$currentKey #6, id=${sendData.id}")
      }
//            key.removeListen(KeyListenFlags.WRITE)
    }
  }

  override fun readyForRead(key: SelectorKey) {
    if (checkConnect()) {
      logger.info(method = "readyForRead") { "not connected" }
      return
    }
    readLock.lock()
    val continuation = readData.continuation
    val data = readData.data
    if (continuation == null) {
      if (currentKey.watching) {
        println("TcpConnection::readyForRead, currentKey=$currentKey no need to read!")
      }
      logger.info(method = "readyForRead") { "no any continuation defined. Cleaning keys" }
      readLock.unlock()
      return
    }
    data ?: error("readData.data is not set")
    val wasRead = channel.receive(data)
    if (wasRead == -1) {
      logger.info(method = "readyForRead") { "during reading find that connection lost" }
      readData.reset()
      readLock.unlock()
      close()
      continuation.resumeWithException(SocketClosedException("Read -1"))
      return
    }
    if (wasRead == 0) {
      readLock.unlock()
      logger.info(method = "readyForRead") { "readyForRead:: no data for read. Try to wait a data" }
      currentKey.addListen(ListenFlags.READ + ListenFlags.ONCE + ListenFlags.ERROR)
      return
    }
    if (readData.full) {
      if (data.remaining == 0) {
        readData.reset()
        readLock.unlock()
        continuation.resume(value = DataTransferSize.ofSize(wasRead))
      } else {
        currentKey.addListen(ListenFlags.READ + ListenFlags.ONCE + ListenFlags.ERROR)
      }
    } else {
      logger.info(method = "readyForRead") { "suspend reading was success. Was read $wasRead bytes" }
      readData.reset()
      readLock.unlock()
      continuation.resume(value = DataTransferSize.ofSize(wasRead))
    }
  }

  override fun error() {
    val connect = this.connect
    logger.info(method = "error") { "Happened!" }
    if (connect != null) {
      logger.info(method = "error") { "Connect error" }
      this.connect = null
      close()
      connect.resumeWithException(SocketConnectException())
      return
    }
    error = true

    if (readData.continuation != null) {
      logger.info(method = "error") { "stopped reading" }
      val c = readLock.synchronize {
        val c = readData.continuation
        readData.reset()
        c
      }
      c?.resumeWith(Result.failure(SocketClosedException()))
    }
    if (sendData.continuation != null) {
      logger.info(method = "error") { "stopped sending" }
      val c = sendData.continuation
      sendData.reset()
      c?.resumeWith(Result.failure(SocketClosedException()))
    }
  }

  override suspend fun connection() {
    logger.info(method = "connection") { "start connect process" }
    val connect = this.connect
    check(connect == null) { "Connection already try connect" }
    suspendCancellableCoroutine<Unit> {
      it.invokeOnCancellation {
        this.connect = null
        if (this.currentKey.updateListenFlags(ListenFlags.ZERO)) {
          this.currentKey.selector.wakeup()
        } else {
          closeAnyway()
        }
      }

//            try {
      this.connect = it
      if (this.currentKey.updateListenFlags(ListenFlags.WRITE + ListenFlags.ERROR + ListenFlags.ONCE)) {
        it.resumeOnException {
          this.currentKey.selector.wakeup()
        }
      } else {
        it.executeAndResumeWithException(ClosedException()) {
          close()
        }
        return@suspendCancellableCoroutine
      }
//            } catch (e: Throwable) {
//                readState = ConnectionState.CLOSED
//                this.connect = null
//                this.currentKey.updateListenFlags(0)
//                it.resumeWithException(e)
//            }
    }
  }

  private var error = false

  private fun checkConnect(): Boolean {
    val connect = this.connect
    if (connect != null) {
      this.connect = null
      connect.resume(value = Unit)
      return true
    }
    return false
  }

  override fun close() {
    logger.info(method = "close") { "closing" }
    if (currentKey.isClosed) {
      return
    }
    val connect = connect
    this.connect = null
    connect?.resumeWithException(SocketClosedException())
    val continuation = readLock.synchronize {
      val continuation = readData.continuation
      readData.reset()
      continuation
    }
    val e = SocketClosedException()
    continuation?.resumeWithException(e)
    sendData.exception(e)
    currentKey.close()
    channel.close()
  }

  override suspend fun asyncClose() {
    close()
  }

  override suspend fun write(data: ByteBuffer): DataTransferSize {
    val id = Random.nextInt()
    var lazy = false
    val start = TimeSource.Monotonic.markNow()
    println("TcpConnection::write Start id=$id, time=${start.elapsedNow()}")
    NetworkDetectSlow("TcpConnection.write #0. currentKey: $currentKey id=$id") {
      if (!data.hasRemaining) {
        logSlow("#1 id=$id")
        return DataTransferSize.EMPTY
      }
      logSlow("#2 id=$id")
      check(sendData.continuation == null) { "Connection already has write operation" }
      if (currentKey.isClosed) {
        logSlow("#3 id=$id")
        throw SocketClosedException()
      }
//        check(!currentKey.isClosed) { "Key already closed. channel: $channel" }
      logSlow("#4 id=$id")
      logger.info(method = "write") { "Try to write ${data.remaining} bytes" }
      logSlow("#5 remaining=${data.remaining} bytes, id=$id")
      val wrote = try {
        channel.send(data)
      } catch (e: Throwable) {
        logSlow("#6")
        throw e
      }
      logSlow("#7 wrote=$wrote, id=$id")
      logger.info(method = "write") { "Wrote $wrote bytes" }
      if (wrote > 0) {
        logSlow("#8 id=$id")
        if (data.hasRemaining) {
          logger.info(method = "write") { "Wrote process success but not all data was wrote" }
        } else {
          logger.info(method = "write") { "Wrote process success!" }
        }
        return DataTransferSize.ofSize(wrote)
      }
      logSlow("#9 id=$id")
      if (wrote == -1) {
        logSlow("#10 id=$id")
        logger.info(method = "write") { "Can't write because wrote=-1. Looks like closed tcp" }
        close()
        throw SocketClosedException()
      }
      logSlow("# mark watching id=$id")
      currentKey.watching = true
      logger.info(method = "write") { "Wrote 0. We should to wait time to write,  id=$id" }
      println("TcpConnection::write suspend write id=$id, time=${start.elapsedNow()}, currentKey=$currentKey")
      val r = suspendCancellableCoroutine<DataTransferSize> {
        sendData.set(
          continuation = it,
          data = data,
          id = id,
          start = start,
        )
        logSlow("#11 id=$id")
        logger.info(method = "write") { "Reset selector to mode: WRITE+ERROR+ONCE" }
        this@TcpConnection.currentKey.addListen(ListenFlags.WRITE + ListenFlags.ERROR + ListenFlags.ONCE)
        this@TcpConnection.currentKey.selector.wakeup()
        it.invokeOnCancellation {
          logger.info(method = "write") { "Write cancelled!" }
//        this.currentKey.removeListen(KeyListenFlags.WRITE)
//        this.currentKey.selector.wakeup()
          sendData.reset()
        }
      }
      if (currentKey.watching && lazy) {
        println("TcpConnection::write Resume id=$id, time=${start.elapsedNow()}, currentKey=$currentKey")
      }
      return r
    }
  }

  override suspend fun flush() {
    // Do nothing
  }

  override val available: Int
    get() = -1

  override suspend fun readFully(dest: ByteBuffer): Int {
    if (dest.remaining == 0) {
      return 0
    }
    if (currentKey.isClosed) {
      throw SocketClosedException()
    }
    if (readData.continuation != null) {
      error("Connection already have read listener")
    }
    val r = channel.receive(dest)
    if (r > 0 && dest.remaining == 0) {
      return r
    }
    if (r == -1) {
      channel.close()
      throw SocketClosedException()
    }
    readData.full = true
    val readed = suspendCancellableCoroutine<DataTransferSize> { continuation ->
      continuation.invokeOnCancellation {
//          currentKey.removeListen(KeyListenFlags.READ)
        readData.reset()
//          currentKey.selector.wakeup()
      }
      readData.set(
        continuation = continuation,
        data = dest,
        id = 0,
        start = TimeSource.Monotonic.markNow(),
      )
      currentKey.addListen(ListenFlags.READ + ListenFlags.ERROR + ListenFlags.ONCE)
      currentKey.selector.wakeup()
    }
    if (readed.isNotAvailable) {
      channel.closeAnyway()
      throw SocketClosedException()
    }
    return readed.length
  }

  override suspend fun read(dest: ByteBuffer): DataTransferSize {
    if (!dest.hasRemaining) {
      logger.info(method = "read") { "dest buffer has no remaining" }
      return DataTransferSize.EMPTY
    }
    if (currentKey.isClosed) {
      logger.info(method = "read") { "SelectorKey is closed. Return connection closed" }
      return DataTransferSize.CLOSED
//      throw SocketClosedException()
    }
    if (readData.continuation != null) {
      logger.info(method = "read") { "continuation not set. Illegal State" }
//      readData.continuation!!.cancel(IllegalStateException("Some other thread wants to read"))
//      readData.continuation = null
      throw IllegalStateException("Connection already have read listener")
    }

    val read = try {
      channel.receive(dest)
    } catch (e: Throwable) {
      logger.info(method = "read") { "optimistic read finished with exception: $e" }
      throw e
    }
    if (read > 0) {
      logger.info(method = "read") { "success optimistic read. $read bytes" }
//      println("TcpConnection.read was read $read")
      return DataTransferSize.ofSize(read)
    }
    if (read == -1) {
      logger.info(method = "read") { "optimistic read found connection lost" }
      channel.close()
      return DataTransferSize.CLOSED
//      throw SocketClosedException()
    }
    logger.info(method = "read") { "optimistic read is failed. Waiting new bytes" }
    readData.full = false
    val wasRead = suspendCancellableCoroutine {
      it.invokeOnCancellation {
//        currentKey.removeListen(KeyListenFlags.READ)
        readData.reset()
//        currentKey.selector.wakeup()
      }
      try {
        readData.set(
          continuation = it,
          data = dest,
          id = 0,
          start = TimeSource.Monotonic.markNow(),
        )
        if (!currentKey.addListen(ListenFlags.READ + ListenFlags.ONCE + ListenFlags.ERROR)) {
          readData.reset()
          it.resumeWithException(SocketClosedException())
          return@suspendCancellableCoroutine
        }
        currentKey.selector.wakeup()
      } catch (e: Throwable) {
        it.resumeWithException(e)
      }
    }
//    println("TcpConnection.read was read $wasRead via suspend")
    return wasRead
  }
}

class SlowContext {
  val text = StringBuilder()
  fun logSlow(text: String) {
    this.text.appendLine(text)
  }
}

inline fun <T> NetworkDetectSlow(msg: String, duration: Duration = 1.seconds, func: SlowContext.() -> T): T {
  val stackTrace = Throwable()
  val finished = AtomicBoolean(false)
  val timeout = AtomicBoolean(false)
  val e = SlowContext()
  GlobalScope.launch {
    delay(duration)
    if (!finished.getValue()) {
      InternalLog.warn(file = "Network") { "Slow: $msg\n${stackTrace.stackTraceToString()}" }
      timeout.setValue(true)
      println("Network---->Start: $msg\n${stackTrace.stackTraceToString()}\n\n${e.text}")
    }
  }

  return try {
    val r = func(e)
    if (timeout.getValue()) {
      println("Network---->End Success: $msg\n${stackTrace.stackTraceToString()}")
    }
    r
  } catch (e: Throwable) {
    if (timeout.getValue()) {
      println("Network---->End With Error: $msg\n${stackTrace.stackTraceToString()}\n\nError:\n ${e.stackTraceToString()}")
    }
    throw e
  } finally {
    finished.setValue(true)
  }
}
