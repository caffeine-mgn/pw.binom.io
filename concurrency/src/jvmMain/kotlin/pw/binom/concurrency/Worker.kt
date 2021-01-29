package pw.binom.concurrency

import pw.binom.Future
import pw.binom.atomic.AtomicInt
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val currentWorker = ThreadLocal<Worker>()
private val idSeq = AtomicLong(0)

actual class Worker actual constructor(name: String?) {
    private val _id = idSeq.incrementAndGet()
    private val worker = Executors.newSingleThreadExecutor()

    init {
        worker.submit {
            currentWorker.set(this)
        }
    }

    @OptIn(ExperimentalTime::class)
    actual fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT> {
        if (worker.isShutdown or worker.isTerminated) {
            throw IllegalStateException("Worker already terminated")
        }
        val future = worker.submit(
            Callable {
                _taskCount.increment()
                val gg = measureTimedValue { runCatching { func(input) } }
                _taskCount.decrement()
                gg.value
            }
        )
        return FutureWrapper(future)
    }

    actual fun requestTermination(): Future<Unit> = execute(Unit) {
        worker.submit {
            currentWorker.set(null)
        }

        worker.shutdown()
        while (!worker.isTerminated) {
            worker.awaitTermination(1, TimeUnit.MINUTES)
        }
    }

    @get:JvmName("binomIsInterrupted")
    actual val isInterrupted: Boolean
        get() = worker.isShutdown

    actual companion object {
        actual val current: Worker?
            get() = currentWorker.get()

    }

    @get:JvmName("binomGetId")
    actual val id: Long
        get() = _id
    private var _taskCount = AtomicInt(0)
    actual val taskCount
        get() = _taskCount.value
}

actual fun Worker.Companion.sleep(deley: Long) {
    Thread.sleep(deley)
}