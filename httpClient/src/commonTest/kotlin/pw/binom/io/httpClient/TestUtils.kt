package pw.binom.io.httpClient

import kotlinx.coroutines.*
import pw.binom.url.toURL
import kotlin.time.Duration

const val HTTP_STORAGE_PORT = 7143
val HTTP_STORAGE_URL = "http://127.0.0.1:$HTTP_STORAGE_PORT".toURL()

suspend fun <T> realWithTimeout(timeout: Duration, f: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Default) {
        withTimeout(timeout, f)
    }

suspend fun realDelay(timeMillis: Long) {
    withContext(Dispatchers.Default) {
        delay(timeMillis)
    }
}
