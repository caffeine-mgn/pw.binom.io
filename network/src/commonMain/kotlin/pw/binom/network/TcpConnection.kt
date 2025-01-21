@file:OptIn(ExperimentalCoroutinesApi::class)

package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.InternalLog
import pw.binom.concurrency.synchronize
import pw.binom.io.*
import pw.binom.io.socket.*
import kotlin.coroutines.resume

class TcpConnection(
  val channel: TcpClientSocket,
  private val currentKey: SelectorKey,
) : AbstractConnection(), AsyncChannel, AsyncChannelPair<AsyncInput, AsyncOutput> {

  override val input: AsyncInput
    get() = this
  override val output: AsyncOutput
    get() = this

  private var connectWater: CancellableContinuation<Boolean>? = null

  var description: String? = null

  override val available: Available
    get() = Available.UNKNOWN

  private val logger = InternalLog.file("TcpConnection").prefix { "$currentKey " }

  override fun toString(): String = "TcpConnection($description)"


  override fun ready(key: SelectorKey, flags: ListenFlags) {
    lock.lock()
    val connectWater = connectWater
    val writeWater = writeWater
    val readWater = readWater

    if (flags.isError) {
      this.connectWater = null
      this.writeWater = null
      this.readWater = null
      lock.unlock()
      connectWater?.resume(false)
      writeWater?.resume(false)
      readWater?.resume(false)
      return
    }

    if (connectWater != null && flags.isReadOrWrite) {
      this.connectWater = null
      lock.unlock()
      connectWater.resume(true)
      return
    }
    val w = if (flags.isWrite) {
      if (writeWater != null) {
        this.writeWater = null
        writeWater
      } else {
        currentKey.removeListen(ListenFlags.WRITE)
        null
      }
    } else {
      null
    }
    val r = if (flags.isRead) {
      if (readWater != null) {
        logger.info(method = "ready") { "read water found" }
        this.readWater = null
        readWater
      } else {
        logger.info(method = "ready") { "read water not found" }
        currentKey.removeListen(ListenFlags.READ)
        null
      }
    } else {
      null
    }

    lock.unlock()
    w?.resume(true)
    r?.resume(true)
  }

  override suspend fun connection() {
//    println("TcpConnection::connection try connect")
    val success = suspendCancellableCoroutine<Boolean> {
      it.invokeOnCancellation {
        lock.synchronize {
          connectWater = null
        }
      }
      lock.synchronize {
        connectWater = it
      }
      currentKey.addListen(ListenFlags.READ + ListenFlags.WRITE)
      currentKey.selector.wakeup()
    }
//    println("TcpConnection::connection connect finished. success=$success")
    if (!success) {
      currentKey.close()
      channel.close()
      throw SocketConnectException()
    }
  }

  override fun close() {
    currentKey.close()
    channel.close()
    lock.lock()
    val writeWater = writeWater
    val readWater = readWater
    val connectWater = connectWater
    this.writeWater = null
    this.readWater = null
    this.connectWater = null
    lock.unlock()
    writeWater?.resume(false)
    readWater?.resume(false)
    connectWater?.resume(false)
  }

  override suspend fun asyncClose() {
    close()
  }

  override suspend fun write(data: ByteBuffer): DataTransferSize {
    if (!data.hasRemaining) {
      return DataTransferSize.EMPTY
    }
    while (true) {
      val l = channel.send(data)
      if (l > 0) {
//        println("TcpConnection::write wrote $l bytes")
        return DataTransferSize.ofSize(l)
      }
      if (l <= -1) {
//        println("TcpConnection::write connection closed")
        currentKey.close()
        channel.close()
        return DataTransferSize.CLOSED
      }

//      currentKey.watching = true
//      println("TcpConnection::write lazy write")
      val success = suspendCancellableCoroutine {
        it.invokeOnCancellation {
          lock.synchronize {
            writeWater = null
          }
        }
        lock.synchronize {
          writeWater = it
        }
        currentKey.addListen(ListenFlags.WRITE)
        currentKey.selector.wakeup()
      }
      if (!success) {
        return DataTransferSize.CLOSED
      }
    }
  }

  override suspend fun read(dest: ByteBuffer): DataTransferSize {
    if (!dest.hasRemaining) {
      return DataTransferSize.EMPTY
    }
    if (currentKey.isClosed){
      return DataTransferSize.CLOSED
    }
    logger.info(method = "read") { "Call read into (${dest.remaining})" }
    while (true) {
      val l = channel.receive(dest)
      logger.info(method = "read") { "Was read $l bytes" }
      if (l > 0) {
//        println("TcpConnection::read was read $l bytes")
        return DataTransferSize.ofSize(l)
      }
      if (l <= -1) {
        logger.info(method = "read") { "Socket closed!" }
//        println("TcpConnection::read connection closed")
        currentKey.close()
        channel.close()
        return DataTransferSize.CLOSED
      }

//      currentKey.watching = true
//      println("TcpConnection::read lazy read")
      val success = suspendCancellableCoroutine {
        it.invokeOnCancellation {
          lock.synchronize {
            readWater = null
          }
        }
        lock.synchronize {
          readWater = it
        }
        logger.info(method = "read") { "Add read flag to socket selector" }
        currentKey.addListen(ListenFlags.READ)
        currentKey.selector.wakeup()
      }
      if (!success) {
        return DataTransferSize.CLOSED
      }
    }
  }

  override suspend fun flush() {
    // Do nothing
  }
}
