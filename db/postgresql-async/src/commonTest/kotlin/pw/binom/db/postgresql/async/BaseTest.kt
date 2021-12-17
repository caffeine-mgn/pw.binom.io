package pw.binom.db.postgresql.async

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import pw.binom.charset.Charsets
import pw.binom.concurrency.sleep
import pw.binom.io.IOException
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.testContainer.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

abstract class BaseTest {
    class PostgresContainer(reuse: Boolean) : TestContainer(
        image = "postgres:11",
        environments = mapOf(
            "POSTGRES_USER" to "postgres",
            "POSTGRES_PASSWORD" to "postgres",
            "POSTGRES_DB" to "test"
        ),
        ports = listOf(
            Port(internalPort = 5432)
        ),
        reuse = reuse,
    )

    val pgContainer = PostgresContainer(reuse = false)

    @OptIn(ExperimentalTime::class)
    fun pg(func: suspend (PGConnection) -> Unit) = runTest {
        val now = TimeSource.Monotonic.markNow()
        pgContainer {
            sleep(1000)
            do {
                val address = NetworkAddress.Immutable(
                    host = "127.0.0.1",
                    port = pgContainer.ports[0].externalPort,
                )
                val connection = try {
                    println("Connection to docker...")
                    PGConnection.connect(
                        address = address,
                        charset = Charsets.UTF8,
                        userName = "postgres",
                        password = "postgres",
                        dataBase = "test"
                    )
                } catch (e: IOException) {
                    if (now.elapsedNow() > Duration.seconds(10)) {
                        throw RuntimeException("Startup Timeout", e)
                    }
                    println("Postgres not available yet")
                    delay(1.seconds)
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