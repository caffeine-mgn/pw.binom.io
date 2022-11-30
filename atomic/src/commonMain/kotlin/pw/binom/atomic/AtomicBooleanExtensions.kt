package pw.binom.atomic

import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
inline fun <T> AtomicBoolean.synchronize(func: () -> T): T {
    val bb = TimeSource.Monotonic.markNow()
    while (true) {
        if (compareAndSet(false, true)) {
            break
        }
        if (bb.elapsedNow() > 10.seconds) {
            println("SpinLock->Lock timeout!!!\n${Throwable().stackTraceToString()}")
        }
    }
    try {
        return func()
    } finally {
        setValue(false)
    }
}
