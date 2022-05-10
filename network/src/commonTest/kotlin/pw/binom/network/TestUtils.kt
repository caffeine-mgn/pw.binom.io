package pw.binom.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration

suspend fun realDelay(d: Duration) {
    withContext(Dispatchers.Default) { delay(d) }
}
