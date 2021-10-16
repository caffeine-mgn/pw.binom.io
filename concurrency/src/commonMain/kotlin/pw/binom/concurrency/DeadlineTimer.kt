package pw.binom.concurrency

import pw.binom.TimeoutException
import pw.binom.io.Closeable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
interface DeadlineTimer : Closeable {
    /**
     * @param delay delay in milliseconds
     * @param func function for call after [delay]
     */
    fun delay(delay: Duration, func: () -> Unit)

    /**
     * Starts [func] in current thread. If [func] can't execute in [delay] duration will throw [TimeoutException]
     */
    suspend fun <T> timeout(delay: Duration, func: suspend () -> T): T

    suspend fun delay(delay: Duration)

    companion object {
        fun create(errorProcessing: ((Throwable) -> Unit)? = null) =
            DeadlineTimerImpl(errorProcessing = errorProcessing)
    }
}