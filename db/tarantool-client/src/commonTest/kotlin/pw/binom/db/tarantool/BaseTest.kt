package pw.binom.db.tarantool

import pw.binom.concurrency.DeadlineTimer
import pw.binom.io.IOException
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.testContainer.TestContainer
import pw.binom.testContainer.invoke
import kotlin.time.Duration
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
    fun pg(func: suspend (TarantoolConnectionImpl) -> Unit) {
        val now = TimeSource.Monotonic.markNow()
        val manager = NetworkDispatcher()
        val dt = DeadlineTimer.create()
        println("Start taratool")
        TarantoolContainer {
            manager.runSingle {
                dt.delay(Duration.seconds(1))
                do {
                    val address = NetworkAddress.Immutable(
                        host = "127.0.0.1",
                        port = TarantoolContainer.ports[0].externalPort,
                    )
                    val connection = try {
                        println("Connection to docker...")
                        TarantoolConnectionImpl.connect(
                            address = address,
                            manager = manager,
                            userName = "server",
                            password = "server",
                        )
                    } catch (e: IOException) {
                        if (now.elapsedNow() > Duration.seconds(10)) {
                            throw RuntimeException("Startup Timeout", e)
                        }
                        println("Postgres not available yet")
                        dt.delay(Duration.seconds(1))
                        continue
                    }
                    println("Connected!")
                    try {
                        connection.use { con ->
                            println("Connected to db")
                            println("Start test function")
                            func(con)
                        }
                    } finally {
                        break
                    }
                } while (true)
            }
        }
    }
}