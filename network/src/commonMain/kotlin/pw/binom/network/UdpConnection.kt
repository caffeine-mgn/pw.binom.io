package pw.binom.network

import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.socket.*
import pw.binom.io.use
import kotlin.coroutines.resumeWithException

class UdpConnection(val channel: UdpNetSocket) : AbstractConnection() {
  companion object {
    fun randomPort() =
      UdpNetSocket().use {
        it.bind(InetSocketAddress.resolve(host = "127.0.0.1", port = 0))
        it.port!!
      }
  }

  var description: String? = null

  override fun toString(): String =
    if (description == null) {
      "UdpConnection"
    } else {
      "UdpConnection($description)"
    }

  val keys = KeyCollection()

  private val readData = InternalUdlReadData()
  private val sendData = InternalUdpSendData()

  fun bind(address: InetSocketAddress) {
    if (channel.bind(address) != BindStatus.OK) {
      throw IOException("Can't bind to $address")
    }
  }

  val port
    get() = channel.port

  override fun readyForWrite(key: SelectorKey) {
    if (sendData.continuation == null) {
      return
    }

    val result = runCatching { channel.send(sendData.data!!, sendData.address!!) }
    if (result.isFailure) {
      val con = sendData.continuation!!
      sendData.reset()
      con.resumeWithException(IOException("Can't send data."))
    } else {
      if (result.getOrNull()!! <= 0) {
        return
      }
    }
    if (sendData.data!!.remaining == 0) {
      val con = sendData.continuation!!
      sendData.reset()
      con.resumeWith(result)
    }
  }

  override suspend fun connection() {
    throw RuntimeException("Not supported")
  }

  override fun error() {
    throw RuntimeException("Not supported")
  }

//    override fun cancelSelector() {
//        sendData.continuation?.cancel()
//        sendData.continuation = null
//        sendData.data = null
//        readData.continuation?.cancel()
//        readData.continuation = null
//        readData.data = null
//    }

  override fun readyForRead(key: SelectorKey) {
    readData.lock()
    if (readData.continuation == null) {
      readData.unlock()
      return
    }
    val readed = runCatching { channel.receive(readData.data!!, readData.address) }
    if (readed.isFailure) {
      readData.unlock()
      throw readed.exceptionOrNull()!!
    }
    if (readData.full) {
      if (readData.data!!.remaining == 0) {
        val con = readData.continuation!!
        readData.reset()
        readData.unlock()
        con.resumeWith(readed)
      }
    } else {
      val con = readData.continuation!!
      readData.reset()
      readData.unlock()
      con.resumeWith(readed)
    }
  }

  override fun close() {
    readData.synchronize {
      readData.continuation?.resumeWithException(SocketClosedException())
      readData.reset()
    }
    sendData.synchronize {
      sendData.continuation?.resumeWithException(SocketClosedException())
      sendData.reset()
    }
    keys.close()
    channel.close()
  }

  suspend fun read(
    dest: ByteBuffer,
    address: MutableInetSocketAddress?,
  ): Int {
    keys.checkEmpty()
    readData.lock()
    if (readData.continuation != null) {
      readData.unlock()
      throw IllegalStateException("Connection already have read listener")
    }
    if (dest.remaining == 0) {
      readData.unlock()
      return 0
    }
    val r = channel.receive(dest, address)
    if (r > 0) {
      readData.unlock()
      return r
    }
    readData.full = false
    readData.unlock()
    val readed =
      suspendCancellableCoroutine<Int> {
        readData.synchronize {
          readData.continuation = it
          readData.data = dest
          readData.address = address
        }
        keys.addListen(ListenFlags.READ + ListenFlags.ERROR)
        keys.wakeup()
        it.invokeOnCancellation {
          readData.continuation = null
          readData.data = null
          readData.address = null
          keys.removeListen(ListenFlags.READ)
        }
      }
    if (readed < 0) {
      throw SocketClosedException()
    }
    return readed
  }

  suspend fun write(
    data: ByteBuffer,
    address: InetSocketAddress,
  ): Int {
    keys.checkEmpty()
    val l = data.remaining
    if (l == 0) {
      return 0
    }

    if (sendData.continuation != null) {
      throw IllegalStateException("Connection already has write listener")
    }
    val wrote = channel.send(data, address)
    if (wrote > 0) {
      return wrote
    }

    sendData.data = data
    sendData.address = address
    suspendCancellableCoroutine<Int> {
      sendData.continuation = it
      keys.addListen(ListenFlags.WRITE + ListenFlags.ERROR)
      keys.wakeup()
    }
    return l
  }
}
