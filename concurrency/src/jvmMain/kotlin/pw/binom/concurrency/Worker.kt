package pw.binom.concurrency

import pw.binom.Future
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.Future as JFuture

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

    actual fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT> {
        if (worker.isShutdown or worker.isTerminated)
            throw IllegalStateException("Worker already terminated")

        val future = worker.submit {
            runCatching { func(input) }
        } as JFuture<Result<RESULT>>
        return FutureWrapper(future)
    }

    actual fun requestTermination(): Future<Unit> = execute(Unit) {
        worker.submit {
            currentWorker.set(null)
        }
        worker.shutdown()
        while (worker.isTerminated) {
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
}

actual fun Worker.Companion.sleep(deley: Long) {
    Thread.sleep(deley)
}