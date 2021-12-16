package pw.binom.db.tarantool

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import pw.binom.io.IOException
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcherImpl
import pw.binom.testContainer.TestContainer
import pw.binom.testContainer.invoke
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

abstract class BaseTest {
    object TarantoolContainer : TestContainer(
        image = "tarantool/tarantool:2.6.2",
        environments = mapOf(
            "TARANTOOL_USER_NAME" to "server",
            "TARANTOOL_USER_PASSWORD" to "server",
        ),
        ports = listOf(
            Port(internalPort = 3301)
        ),
        reuse = true,
    )

    @OptIn(ExperimentalTime::class)
    fun pg(func: suspend (TarantoolConnectionImpl) -> Unit) = runBlocking {
        val now = TimeSource.Monotonic.markNow()
        val manager = NetworkCoroutineDispatcherImpl()
        TarantoolContainer {
            delay(1.seconds)
            do {
                val address = NetworkAddress.Immutable(
                    host = "127.0.0.1",
                    port = TarantoolContainer.ports[0].externalPort,
                )
                val connection = try {
                    TarantoolConnection.connect(
                        address = address,
                        manager = manager,
                        userName = "server",
                        password = "server",
                    )
                } catch (e: IOException) {
                    if (now.elapsedNow() > 10.seconds) {
                        throw RuntimeException("Startup Timeout", e)
                    }
                    delay(1.seconds)
                    continue
                }
                try {
                    connection.use { con ->
                        func(con)
                    }
                } finally {
                    break
                }
            } while (true)
        }
    }
}