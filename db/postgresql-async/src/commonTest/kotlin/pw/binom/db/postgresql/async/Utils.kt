package pw.binom.db.postgresql.async

import pw.binom.charset.Charsets
import pw.binom.io.use
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.seconds
/*
@OptIn(ExperimentalTime::class)
fun pg(func: suspend (PGConnection) -> Unit) {
    val manager = NetworkDispatcher()
    val done = manager.startCoroutine {
        val address = NetworkAddress.Immutable(
            host = "127.0.0.1",
            port = 25331,
        )
        PGConnection.connect(
            address = address,
            networkDispatcher = manager,
            charset = Charsets.UTF8,
            userName = "postgres",
            password = "postgres",
            dataBase = "test"
        ).use { con ->
            println("Connected to db")
            println("Start test function")
            func(con)
        }
    }

    val now = TimeSource.Monotonic.markNow()
    while (true) {
        if (done.isDone && done.isFailure) {
            throw done.exceptionOrNull!!
        }
        if (now.elapsedNow() > 10.seconds) {
            throw RuntimeException("Out of try")
        }
        if (done.isDone) {
            break
        }
        manager.select(1000)
    }
}
*/