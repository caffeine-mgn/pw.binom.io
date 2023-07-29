package pw.binom.thread

import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ConcurrentQueue
import pw.binom.io.ClosedException

internal val maker: () -> Unit = {}

abstract class AbstractThreadExecutorService : ExecutorService {
  protected val queue = ConcurrentQueue<() -> Unit>()

  protected val closed = AtomicBoolean(false)

  override val taskCount: Int
    get() = queue.size

  override val isShutdown
    get() = closed.getValue()


  override fun submit(f: () -> Unit) {
    if (closed.getValue()) {
      throw ClosedException()
    }
    queue.push(f)
  }

  protected abstract fun joinAllThread()
  protected fun pushBreakMessage() {
    queue.push(maker)
  }

  override fun shutdownNow(): Collection<() -> Unit> {
    if (!closed.compareAndSet(false, true)) {
      throw ClosedException()
    }
    val list = ArrayList<() -> Unit>(queue.size)
    joinAllThread()
    while (!queue.isEmpty) {
      val func = queue.pop()
      if (func === maker) {
        continue
      }
      list += func
    }
    return list
  }
}
