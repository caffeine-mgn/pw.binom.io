package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import pw.binom.executeAndResumeWithException
import pw.binom.io.ClosedException
import pw.binom.io.socket.*
import pw.binom.io.use
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TcpServerConnection constructor(
  val dispatcher: NetworkManager,
  val channel: TcpNetServerSocket,
  private var currentKey: SelectorKey,
) : AbstractConnection() {
  var description: String? = null

  companion object {
    fun randomPort() =
      Socket.createTcpServerNetSocket().use {
        it.bind(InetNetworkAddress.create(host = "127.0.0.1", port = 0))
        it.port!!
      }
  }

  override fun toString(): String =
    if (description == null) {
      "TcpServerConnection"
    } else {
      "TcpServerConnection($description)"
    }

  override fun readyForWrite(key: SelectorKey) {
    // Do nothing
  }

  override suspend fun connection() {
    error()
  }

  val port
    get() = channel.port!!

  private var closed = false

  override fun error() {
    channel.closeAnyway()
    currentKey.closeAnyway()
    val acceptListener = acceptListener
    if (acceptListener != null) {
      this.acceptListener = null
      acceptListener.resumeWithException(SocketClosedException())
    }
    // ignore error
//        close()
  }

  override fun readyForRead(key: SelectorKey) {
//        println("TcpServerConnection::readyForRead")
    val acceptListener = acceptListener ?: return
    val newChannel = channel.accept(null)
    if (newChannel == null) {
      safeUpdate(acceptListener, KeyListenFlags.READ or KeyListenFlags.ONCE or KeyListenFlags.ERROR)
//            println("TcpServerConnection::readyForRead newChannel == null")
      return
    }
    this.acceptListener = null
    acceptListener.resume(newChannel)
  }

  override fun close() {
    val acceptListener = acceptListener
    this.acceptListener = null
    acceptListener?.also {
      if (it.isActive) {
        it.resumeWithException(SocketClosedException())
      }
    }
    if (!currentKey.isClosed) {
      currentKey.close()
    }
    channel.close()
  }

  private var acceptListener: CancellableContinuation<TcpClientSocket>? = null

  private fun safeUpdate(
    con: CancellableContinuation<TcpClientSocket>,
    flags: Int,
  ) = if (currentKey.updateListenFlags(flags)) {
    currentKey.selector.wakeup()
    true
  } else {
    con.executeAndResumeWithException(ClosedException()) {
      close()
    }
    false
  }

  suspend fun accept(address: MutableInetNetworkAddress? = null): TcpConnection {
    if (closed) {
//            println("TcpServerConnection::accept closed==true")
      throw SocketClosedException()
    }
    check(acceptListener == null) { "Connection already have read listener" }

    val newClient = channel.accept(address)
    if (newClient != null) {
//            println("TcpServerConnection::accept accepted!!!")
      return dispatcher.attach(newClient).also {
        it.description = "Server of $description"
      }
    }
    val newChannel =
      suspendCancellableCoroutine<TcpClientSocket> { con ->
        try {
          acceptListener = con
          safeUpdate(con, KeyListenFlags.READ or KeyListenFlags.ONCE or KeyListenFlags.ERROR)
        } catch (e: Throwable) {
          con.resumeWithException(e)
        }
        con.invokeOnCancellation {
//                println("TcpServerConnection::accept canceled waiting")
          acceptListener = null
          if (currentKey.updateListenFlags(0)) {
            currentKey.selector.wakeup()
          } else {
            close()
          }
        }
      }
    return dispatcher.attach(newChannel).also {
      it.description = "Server of $description"
    }
  }
}
