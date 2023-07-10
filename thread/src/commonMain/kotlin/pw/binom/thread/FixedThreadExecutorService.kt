package pw.binom.thread

import pw.binom.atomic.AtomicLong
import pw.binom.collections.defaultMutableSet

class FixedThreadExecutorService(
  threadCount: Int,
  val threadFactory: ((Thread) -> Unit) -> Thread = { func -> Thread(func = func) },
) : AbstractThreadExecutorService() {

  init {
    require(threadCount >= 1) { "threadCount should be more or equals 1" }
  }

  private val taskCounter = AtomicLong(0)

  private val threadFunc: (Thread) -> Unit = { thread ->
    while (!closed.getValue()) {
//      val id = taskCounter.addAndGet(1)
      try {
        val func = queue.popBlocked()
        func.invoke()
      } catch (e: Throwable) {
        thread.uncaughtExceptionHandler.uncaughtException(thread = thread, throwable = e)
      } finally {
      }
    }
  }

  private val threads = Array(threadCount) {
    threadFactory(threadFunc)
  }

  private val allThreadIds = defaultMutableSet<Long>()

  init {
    threads.forEach {
      it.start()
      allThreadIds += it.id
    }
  }

  override fun joinAllThread() {
    repeat(threads.size) {
      pushBreakMessage()
    }
    threads.forEach {
      it.join()
    }
  }

  override fun isThreadFromPool(thread: Thread): Boolean = thread.id in allThreadIds
}
