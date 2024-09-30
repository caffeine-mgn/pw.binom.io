@file:OptIn(ExperimentalCoroutinesApi::class)

package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.InternalLog
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

class TcpConnection(
  val channel: TcpClientSocket,
  private val currentKey: SelectorKey,
) : AbstractConnection(), AsyncChannel {
  private var connect: CancellableContinuation<Unit>? = null
  var description: String? = null

  private val logger = InternalLog.file("TcpConnection").prefix { "TcpConnection@${hashCode()} " }

  override fun toString(): String = "TcpConnection($description)"

  private class IOState {
    var continuation: CancellableContinuation<DataTransferSize>? = null
    var data: ByteBuffer? = null
    var full = false

    fun reset() {
      continuation = null
      data = null
    }

    fun set(
      continuation: CancellableContinuation<DataTransferSize>,
      data: ByteBuffer,
    ) {
      this.continuation = continuation
      this.data = data
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
    if (checkConnect()) {
      return
    }
    if (sendData.continuation != null) {
      val result = runCatching { DataTransferSize.ofSize(channel.send(sendData.data!!)) }
      if (result.isSuccess && result.getOrNull()!!.isNotAvailable) {
        sendData.continuation?.cancel(SocketClosedException())
        sendData.reset()
        return
      }
      if (sendData.data!!.remaining == 0) {
        val con = sendData.continuation!!
        sendData.reset()
//                key.removeListen(KeyListenFlags.WRITE)
        con.resumeWith(result)
      } else {
        key.addListen(ListenFlags.WRITE + ListenFlags.ERROR + ListenFlags.ONCE)
      }
      if (sendData.continuation == null) {
        if (!currentKey.updateListenFlags(calcListenFlags())) {
          closeAnyway()
        }
      }
    } else {
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
    val oldRemaining = data.remaining
    if (oldRemaining == 0) {
      return DataTransferSize.EMPTY
    }
    if (sendData.continuation != null) {
      error("Connection already has write operation")
    }
    if (currentKey.isClosed) {
      throw SocketClosedException()
    }
//        check(!currentKey.isClosed) { "Key already closed. channel: $channel" }
    val wrote = channel.send(data)
    if (wrote > 0) {
      return DataTransferSize.ofSize(wrote)
    }
    if (wrote == -1) {
      close()
      throw SocketClosedException()
    }
    sendData.data = data
    return suspendCancellableCoroutine<DataTransferSize> {
      sendData.continuation = it
      this.currentKey.addListen(ListenFlags.WRITE + ListenFlags.ERROR + ListenFlags.ONCE)
      this.currentKey.selector.wakeup()
      it.invokeOnCancellation {
//        this.currentKey.removeListen(KeyListenFlags.WRITE)
//        this.currentKey.selector.wakeup()
        sendData.reset()
      }
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
