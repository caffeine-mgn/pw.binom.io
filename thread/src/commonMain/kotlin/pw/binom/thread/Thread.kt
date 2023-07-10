package pw.binom.thread

import pw.binom.BinomMetrics
import pw.binom.metric.MutableLongGauge
import kotlin.time.Duration

internal object ThreadMetrics {
  private val threadCount = MutableLongGauge("binom_thread_count", description = "Thread Count")
  fun incThread() {
    threadCount.inc()
  }

  fun decThread() {
    threadCount.dec()
  }

  init {
    BinomMetrics.reg(threadCount)
  }
}

fun Thread(func: (Thread) -> Unit) = object : Thread() {
  override fun execute() {
    func(this)
  }
}

fun Thread(name: String, func: (Thread) -> Unit) = object : Thread(name) {
  override fun execute() {
    func(this)
  }
}

expect abstract class Thread {
  companion object {
    val currentThread: Thread
    fun sleep(millis: Long)
    fun sleep(duration: Duration)
    fun yield()
  }

  constructor()
  constructor(name: String)

  val isActive: Boolean
  var name: String
  val id: Long
  var uncaughtExceptionHandler: UncaughtExceptionHandler
  abstract fun execute()
  fun start()
  fun join()
}
