package pw.binom.concurrency

import pw.binom.io.Closeable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
@Deprecated(message = "Not use it. Will be deleted")
@OptIn(ExperimentalTime::class)
interface DeadlineTimer : Closeable {
    /**
     * @param delay delay in milliseconds
     * @param func function for call after [delay]
     */
    fun delay(delay: Duration, func: () -> Unit)
    suspend fun delay(delay: Duration)

    companion object {
        fun create(errorProcessing: ((Throwable) -> Unit)? = null) =
            DeadlineTimerImpl(errorProcessing = errorProcessing)
    }
}