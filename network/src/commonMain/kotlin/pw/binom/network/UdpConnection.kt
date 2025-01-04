package pw.binom.network

import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.InternalLog
import pw.binom.concurrency.synchronize
import pw.binom.io.ByteBuffer
import pw.binom.io.DataTransferSize
import pw.binom.io.IOException
import pw.binom.io.socket.*
import pw.binom.io.use
import kotlin.coroutines.resume

class UdpConnection(
  val channel: UdpNetSocket,
  private val currentKey: SelectorKey,
) : AbstractConnection() {
  companion object {
    fun randomPort() =
      UdpNetSocket().use {
        it.bind(InetSocketAddress.resolve(host = "127.0.0.1", port = 0))
        it.port!!
      }
  }

  private val logger = InternalLog.file("UdpConnection").prefix { "$currentKey " }

  var description: String? = null

  override fun toString(): String =
    if (description == null) {
      "UdpConnection"
    } else {
      "UdpConnection($description)"
    }

  fun bind(address: InetSocketAddress) {
    if (channel.bind(address) != BindStatus.OK) {
      throw IOException("Can't bind to $address")
    }
  }

  val port
    get() = channel.port

  override suspend fun connection() {
    throw RuntimeException("Not supported")
  }

  override fun error() {
    throw RuntimeException("Not supported")
  }

  override fun ready(key: SelectorKey, flags: ListenFlags) {
    lock.lock()
    val writeWater = writeWater
    val readWater = readWater

    if (flags.isError) {
      this.writeWater = null
      this.readWater = null
      lock.unlock()
      writeWater?.resume(false)
      readWater?.resume(false)
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


  override fun close() {
    currentKey.close()
    channel.close()
    lock.lock()
    val writeWater = writeWater
    val readWater = readWater
    this.writeWater = null
    this.readWater = null
    lock.unlock()
    writeWater?.resume(false)
    readWater?.resume(false)
  }

  suspend fun read(
    dest: ByteBuffer,
    address: MutableInetSocketAddress? = null,
  ): DataTransferSize {
    if (!dest.hasRemaining) {
      return DataTransferSize.EMPTY
    }
    if (currentKey.isClosed) {
      return DataTransferSize.CLOSED
    }
    logger.info(method = "read") { "Call read into (${dest.remaining})" }
    while (true) {
      val l = channel.receive(dest, address)
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

  suspend fun write(
    data: ByteBuffer,
    address: InetSocketAddress,
  ): DataTransferSize {
    if (!data.hasRemaining) {
      return DataTransferSize.EMPTY
    }
    while (true) {
      val l = channel.send(data, address)
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
}
