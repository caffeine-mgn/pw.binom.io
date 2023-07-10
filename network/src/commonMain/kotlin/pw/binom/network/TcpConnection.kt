package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.executeAndResumeWithException
import pw.binom.io.AsyncChannel
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException
import pw.binom.io.socket.*
import pw.binom.resumeOnException
import kotlin.coroutines.resumeWithException

class TcpConnection(
  val channel: TcpClientSocket,
  private val currentKey: SelectorKey,
) : AbstractConnection(), AsyncChannel {
  private var connect: CancellableContinuation<Unit>? = null
  var description: String? = null

  override fun toString(): String =
    "TcpConnection(description: $description, channel: $channel, key: $currentKey)"

  private class IOState {
    var continuation: CancellableContinuation<Int>? = null
    var data: ByteBuffer? = null
    var full = false

    fun reset() {
      continuation = null
      data = null
    }

    fun set(continuation: CancellableContinuation<Int>, data: ByteBuffer) {
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

  private fun calcListenFlags() = when {
    readData.continuation != null && (sendData.continuation != null) -> KeyListenFlags.READ or KeyListenFlags.ERROR or KeyListenFlags.WRITE
    readData.continuation != null -> KeyListenFlags.READ or KeyListenFlags.ERROR
    sendData.continuation != null -> KeyListenFlags.WRITE or KeyListenFlags.ERROR
    else -> 0
  }

  override fun readyForWrite(key: SelectorKey) {
    if (checkConnect()) {
      return
    }
    if (sendData.continuation != null) {
      val result = runCatching { channel.send(sendData.data!!) }
      if (result.isSuccess && result.getOrNull()!! < 0) {
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
        key.addListen(KeyListenFlags.WRITE or KeyListenFlags.ERROR or KeyListenFlags.ONCE)
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
      return
    }

    val continuation = readData.continuation
    val data = readData.data
    if (continuation == null) {
      currentKey.removeListen(KeyListenFlags.READ)
//            println("TcpConnection::readyForRead suspend!. continuation=null. channel: $channel")
      return
    }
    data ?: error("readData.data is not set")
    val readed = channel.receive(data)
    if (readed == -1) {
      readData.reset()
      close()
      continuation.resumeWithException(SocketClosedException("Read -1"))
      return
    }
    if (readed == 0) {
//            println("TcpConnection::readyForRead readed 0. Try ready again later")
      currentKey.addListen(KeyListenFlags.READ or KeyListenFlags.ONCE or KeyListenFlags.ERROR)
//            println("TcpConnection::readyForRead suspend. read 0. channel: $channel")
      return
    }
//        if (readed > 0) {
//            data.holdState {
//                data.position = p
//                data.limit = p + readed
//                println("TcpClient: Read lazy: ${data.toByteArray().decodeToString()}")
//            }
//        }
    if (readData.full) {
      if (data.remaining == 0) {
        readData.reset()
        continuation.resume(value = readed, onCancellation = null)
      } else {
        currentKey.addListen(KeyListenFlags.READ or KeyListenFlags.ONCE or KeyListenFlags.ERROR)
      }
    } else {
      readData.reset()
//            println("TcpConnection::readyForRead readed. $readed bytes. channel: $channel")
      continuation.resume(value = readed, onCancellation = null)
    }
  }

  override fun error() {
    val connect = this.connect
    if (connect != null) {
      this.connect = null
      close()
      connect.resumeWithException(SocketConnectException())
      return
    }
    error = true

    if (readData.continuation != null) {
      val c = readData.continuation
      readData.reset()
      c?.resumeWith(Result.failure(SocketClosedException()))
    }
    if (sendData.continuation != null) {
      val c = sendData.continuation
      sendData.reset()
      c?.resumeWith(Result.failure(SocketClosedException()))
    }
  }

  override suspend fun connection() {
    val connect = this.connect
    check(connect == null) { "Connection already try connect" }
    suspendCancellableCoroutine<Unit> {
      it.invokeOnCancellation {
        this.connect = null
        if (this.currentKey.updateListenFlags(0)) {
          this.currentKey.selector.wakeup()
        } else {
          closeAnyway()
        }
      }

//            try {
      this.connect = it
      if (this.currentKey.updateListenFlags(KeyListenFlags.WRITE or KeyListenFlags.ERROR or KeyListenFlags.ONCE)) {
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
      connect.resume(value = Unit, onCancellation = null)
      return true
    }
    return false
  }

  override fun close() {
    if (currentKey.isClosed) {
      return
    }
    val connect = connect
    this.connect = null
    connect?.resumeWithException(SocketClosedException())
    readData.exception(SocketClosedException())
    sendData.exception(SocketClosedException())
    currentKey.close()
    channel.close()
  }

  override suspend fun asyncClose() {
    close()
  }

  override suspend fun write(data: ByteBuffer): Int {
    val oldRemaining = data.remaining
    if (oldRemaining == 0) {
      return 0
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
      return wrote
    }
    if (wrote == -1) {
      close()
      throw SocketClosedException()
    }
//        println("wait write event...")
    sendData.data = data
    return suspendCancellableCoroutine<Int> {
      sendData.continuation = it
      this.currentKey.addListen(KeyListenFlags.WRITE or KeyListenFlags.ERROR or KeyListenFlags.ONCE)
      this.currentKey.selector.wakeup()
      it.invokeOnCancellation {
        this.currentKey.removeListen(KeyListenFlags.WRITE)
        this.currentKey.selector.wakeup()
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
    val readed = suspendCancellableCoroutine<Int> { continuation ->
      continuation.invokeOnCancellation {
        currentKey.removeListen(KeyListenFlags.READ)
        readData.reset()
        currentKey.selector.wakeup()
      }
      readData.set(
        continuation = continuation,
        data = dest,
      )
      currentKey.addListen(KeyListenFlags.READ or KeyListenFlags.ERROR or KeyListenFlags.ONCE)
      currentKey.selector.wakeup()
    }
    if (readed == 0) {
      channel.closeAnyway()
      throw SocketClosedException()
    }
    return readed
  }

  override suspend fun read(dest: ByteBuffer): Int {
    if (dest.remaining == 0) {
      return 0
    }
    if (currentKey.isClosed) {
      throw SocketClosedException()
    }
    if (readData.continuation != null) {
//      readData.continuation!!.cancel(IllegalStateException("Some other thread wants to read"))
//      readData.continuation = null
      throw IllegalStateException("Connection already have read listener")
    }

    val read = try {
      channel.receive(dest)
    } catch (e: Throwable) {
      throw e
    }
    if (read > 0) {
//            println("TcpConnection::read success. $read bytes. channel: $channel")
      return read
    }
    if (read == -1) {
//            println("TcpConnection::read reading with error. channel: $channel")
      channel.close()
      throw SocketClosedException()
    }
    readData.full = false
//        println("TcpConnection::read suspend reading. channel: $channel")
    return suspendCancellableCoroutine {
      it.invokeOnCancellation {
        currentKey.removeListen(KeyListenFlags.READ)
        readData.reset()
        currentKey.selector.wakeup()
      }
      try {
        readData.set(
          continuation = it,
          data = dest,
        )
        currentKey.addListen(KeyListenFlags.READ or KeyListenFlags.ONCE or KeyListenFlags.ERROR)
        currentKey.selector.wakeup()
      } catch (e: Throwable) {
        it.resumeWithException(e)
      }
    }
  }
}
