package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import pw.binom.concurrency.SpinLock
import pw.binom.io.Closeable
import pw.binom.io.socket.ListenFlags
import pw.binom.io.socket.SelectorKey

abstract class AbstractConnection : Closeable {

  /**
   * Call by SocketNIOManager. Called in network thread when connection ready for write
   *
   * @return should return whether the connection should write more
   */
  open fun readyForWrite(key: SelectorKey) {}
  open fun ready(key: SelectorKey, flags: ListenFlags) {}
  abstract suspend fun connection()
//    abstract fun connecting()

  //    abstract fun connected()
  open fun error() {}

  /**
   * Call by SocketNIOManager. Called in network thread when connection ready for read
   *
   * @return should return, should the connection read more
   */
  open fun readyForRead(key: SelectorKey) {}

  protected val lock = SpinLock()
  protected var writeWater: CancellableContinuation<Boolean>? = null
  protected var readWater: CancellableContinuation<Boolean>? = null
}
