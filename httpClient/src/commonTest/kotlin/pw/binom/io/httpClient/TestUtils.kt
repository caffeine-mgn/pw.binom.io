package pw.binom.io.httpClient

import kotlinx.coroutines.*
import kotlin.time.Duration

suspend fun <T> realWithTimeout(timeout: Duration, f: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Default) {
        withTimeout(timeout, f)
    }

suspend fun realDelay(timeMillis: Long) {
    withContext(Dispatchers.Default) {
        delay(timeMillis)
    }
}