package pw.binom.thread

import pw.binom.uuid
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.random.Random

private val currentWorker = ThreadLocal<Worker>()

actual class Worker actual constructor(name: String?) : Thread(name?:"Thread-${Random.uuid().toShortString()}") {
    class Job<DATA, RESULT>(val input: DATA, val func: (DATA) -> RESULT, val future: WorkerFuture<RESULT>)

    private val lock = ReentrantLock()
    private val con = lock.newCondition()
    private val jobs = LinkedList<Job<Any?, Any?>>()
    private val terminate = AtomicBoolean(false)

    actual fun <DATA, RESULT> execute(input: DATA, func: (DATA) -> RESULT): Future<RESULT> {
        if (terminate.get())
            throw IllegalStateException("Worker already terminated")
        val feature = WorkerFuture<RESULT>()
        lock.lock {
            jobs.addLast(Job(input, func, feature) as Job<Any?, Any?>)
            con.signal()
        }
        return feature
    }

    override fun run() {
        currentWorker.set(this)
        try {
            while (!terminate.get()) {
                val task = lock.lock {
                    if (jobs.isEmpty()) {
                        con.await()
                    }
                    jobs.removeFirst()
                } ?: continue

                val result = runCatching {
                    task.func(task.input)
                }
                task.future.resume(result)
            }
        } finally {
            currentWorker.remove()
        }
    }

    init {
        start()
    }

    actual fun requestTermination() = execute(Unit) {
        terminate.set(true)
    }

    @get:JvmName("binomIsInterrupted")
    actual val isInterrupted: Boolean
        get() = isInterrupted()

    actual companion object {
        actual val current: Worker?
            get() = currentWorker.get()

    }

    @get:JvmName("binomGetId")
    actual val id: Long
        get() = getId()
}

actual fun Worker.Companion.sleep(deley: Long) {
    Thread.sleep(deley)
}