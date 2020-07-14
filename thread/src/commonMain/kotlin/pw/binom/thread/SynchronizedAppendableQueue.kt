package pw.binom.thread

import pw.binom.AppendableQueue
import pw.binom.PopResult
import pw.binom.io.Closeable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

fun <T> AppendableQueue<T>.synchronized() = SynchronizedAppendableQueue(this)

@OptIn(ExperimentalTime::class)
class SynchronizedAppendableQueue<T>(val q: AppendableQueue<T>) : AppendableQueue<T>, Closeable {
    private val lock = Lock()
    private val condition = lock.newCondition()

    override val isEmpty: Boolean
        get() = lock.synchronize {
            q.isEmpty
        }
    override val size: Int
        get() = lock.synchronize {
            q.size
        }

    fun pop(duration: Duration): T? =
            lock.synchronize {
                val now = TimeSource.Monotonic.markNow()
                while (true) {
                    if (now.elapsedNow() > duration)
                        return@synchronize null
                    if (q.isEmpty) {
                        if (condition.wait(duration))
                            return null
                    } else
                        break
                }
                q.pop()
            }

    override fun pop(): T =
            lock.synchronize {
                while (true) {
                    if (q.isEmpty)
                        condition.wait()
                    else
                        break
                }
                q.pop()
            }

    override fun pop(dist: PopResult<T>) {
        lock.synchronize {
            q.pop(dist)
        }
    }

    override fun push(value: T) {
        lock.synchronize {
            q.push(value)
            condition.notify()
        }
    }

    override fun peek(): T =
            lock.synchronize {
                if (isEmpty)
                    condition.wait()
                q.peek()
            }

    override fun close() {
        lock.synchronize {
            condition.close()
            lock.close()
        }
    }

}