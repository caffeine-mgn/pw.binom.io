package pw.binom.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import pw.binom.io.socket.InetNetworkAddress
import kotlin.time.Duration

const val HTTP_SERVER_PORT = 7141
val HTTP_SERVER_ADDRESS = InetNetworkAddress.create(host = "127.0.0.1", port = HTTP_SERVER_PORT)
suspend fun realDelay(d: Duration) {
    withContext(Dispatchers.Default) { delay(d) }
}
