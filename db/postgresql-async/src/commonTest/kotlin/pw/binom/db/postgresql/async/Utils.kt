package pw.binom.db.postgresql.async

import pw.binom.async
import pw.binom.async2
import pw.binom.charset.Charsets
import pw.binom.concurrency.Worker
import pw.binom.concurrency.sleep
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.seconds

@OptIn(ExperimentalTime::class)
fun pg(func: suspend (PGConnection) -> Unit) {
    val manager = NetworkDispatcher()
    var done = false
    var exception: Throwable? = null
    async2 {
        var con: PGConnection
        val now = TimeSource.Monotonic.markNow()
        while (true) {
            try {
                val address = NetworkAddress.Immutable(
                    host = "127.0.0.1",
                    port = 25331,
                )
                con = PGConnection.connect(
                    address = address,
                    manager = manager,
                    charset = Charsets.UTF8,
                    userName = "postgres",
                    password = "postgres",
                    dataBase = "test"
                )
                break
            } catch (e: Throwable) {
                Worker.sleep(500)
                if (now.elapsedNow() > 10.seconds) {
                    exception = RuntimeException("Connection Timeout", e)
                    return@async2
                }
//                    exception = e
                Worker.sleep(100)
//                    e.printStackTrace()
//                    done = true
//                    throw e
            }
        }
        if (!done) {
            try {
                func(con)
            } catch (e: Throwable) {
                exception = e
            } finally {
                con.asyncClose()
                done = true
            }
        }
    }

    val now = TimeSource.Monotonic.markNow()
    while (!done) {
        if (exception != null) {
            throw exception!!
        }
        if (now.elapsedNow() > 10.seconds) {
            throw RuntimeException("Out of try")
        }
        manager.select(1000)
    }
    if (exception != null) {
        throw exception!!
    }
}