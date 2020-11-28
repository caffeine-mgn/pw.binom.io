package pw.binom.concurrency

import pw.binom.BaseFuture
import pw.binom.Future2
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.doFreeze
import kotlin.time.ExperimentalTime
import kotlin.time.milliseconds

class WorkerPool(size: Int, val timeForCheckTask: Int = 200) {
    private class State(size: Int) {
        var interotped by AtomicBoolean(false)
        val stoped = AtomicInt(size)
        val queue = ConcurrentQueue<() -> Any?>()

        init {
            doFreeze()
        }
    }

    private val state = State(size)

    @OptIn(ExperimentalTime::class)
    val list = Array(size) {
        val w = Worker()
        w.execute(state) { state ->
            while (!state.interotped) {
                val task = state.queue.popBlocked(timeForCheckTask.milliseconds)
                        ?: if (state.interotped) {
                            break
                        } else {
                            continue
                        }
                try {
                    task()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            Worker.current!!.requestTermination()
            state.stoped.increment()
        }
        w
    }

    fun shutdown() {
        if (state.interotped) {
            throw IllegalStateException("WorkerPool already has Interotped")
        }
        state.interotped = true
        while (state.stoped.value != 0) {
            Worker.sleep(50)
        }
    }

    fun shutdownNow(): List<() -> Any?> {
        if (state.interotped) {
            throw IllegalStateException("WorkerPool already has Interotped")
        }
        state.interotped = true
        val out = ArrayList<() -> Any?>(state.queue.size)
        while (!state.queue.isEmpty) {
            out += state.queue.pop()
        }
        return out
    }

    fun <T> submit(f: () -> T): Future2<T> {
        val future = BaseFuture<T>()
        state.queue.push {
            future.resume(runCatching(f))
        }
        return future
    }
}