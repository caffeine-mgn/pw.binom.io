package pw.binom.network

import kotlinx.coroutines.CancellableContinuation
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.InetSocketAddress

internal class InternalUdpSendData {
  var continuation: CancellableContinuation<Int>? = null
  var data: ByteBuffer? = null
  var address: InetSocketAddress? = null
  private val lock = SpinLock()

  fun lock() = lock.lock()

  fun unlock() = lock.unlock()

  inline fun <T> synchronize(func: () -> T): T = lock.synchronize(func)

  fun reset() {
    continuation = null
    data = null
    address = null
  }
}
