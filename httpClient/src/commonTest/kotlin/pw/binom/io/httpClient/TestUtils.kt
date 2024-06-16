package pw.binom.io.httpClient

import kotlinx.coroutines.*
import pw.binom.url.toURL
import kotlin.time.Duration

const val HTTP_STORAGE_PORT = 7153
const val HTTP_WS_PORT = 7142
val HTTP_STORAGE_URL = "http://127.0.0.1:$HTTP_STORAGE_PORT".toURL()
val HTTP_WS_URL = "http://127.0.0.1:$HTTP_WS_PORT/".toURL()

suspend fun <T> realWithTimeout(timeout: Duration, f: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.Default) {
        withTimeout(timeout, f)
    }

suspend fun realDelay(time: Duration) {
  withContext(Dispatchers.Default) {
    delay(time)
  }
}

suspend fun realDelay(timeMillis: Long) {
    withContext(Dispatchers.Default) {
        delay(timeMillis)
    }
}
