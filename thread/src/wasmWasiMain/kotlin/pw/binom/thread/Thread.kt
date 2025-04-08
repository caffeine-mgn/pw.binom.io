package pw.binom.thread

import pw.binom.wasm.wasi.wasiSchedYield
import pw.binom.wasm.wasi.wasiThreadSpawn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

private var createCount = 0
private fun genName() = "Thread-${createCount++}"

actual abstract class Thread actual constructor(actual var name: String) {
  actual companion object {
    private var counter = 0
    private val threads = HashMap<Int, Thread>()
    actual val currentThread: Thread
      get() {
        val id = wasiThreadId()
        threads[id]?.let { return it }
        val t = object : Thread() {
          override fun execute() {
            throw IllegalStateException()
          }
        }
        t.name = "Main"
        t.internalId = id
        threads[id] = t
        return t
      }

    actual fun sleep(millis: Long) {
      sleep(millis.milliseconds)
    }

    actual fun sleep(duration: Duration) {
      if (wasm_sleep(duration)) {
        return
      }
      val now = TimeSource.Monotonic.markNow()
      while (now.elapsedNow() > duration) {
        // do nothing
      }
    }

    actual fun yield() {
      wasiSchedYield()
    }
  }

  internal var started = false
  actual val isActive: Boolean
    get() = started
  internal var internalId: Int = 0
  actual val id: Long
    get() = internalId.toLong()
  actual var uncaughtExceptionHandler: UncaughtExceptionHandler = DefaultUncaughtExceptionHandler

  actual abstract fun execute()
  actual fun start() {
    val tmpId = counter++
    threadsForStart[tmpId] = this
    internalId = wasiThreadSpawn(tmpId)
  }

  actual fun join() {
    while (isActive) {
      yield()
    }
  }

  internal fun realStart() {
    try {
      ThreadMetrics.incThread()
      started = true
      threads[internalId] = this
      execute()
    } catch (e: Throwable) {
      uncaughtExceptionHandler.uncaughtException(this, e)
    } finally {
      threads.remove(internalId)
      started = false
      ThreadMetrics.decThread()
    }
  }

  actual constructor() : this(genName())
}
