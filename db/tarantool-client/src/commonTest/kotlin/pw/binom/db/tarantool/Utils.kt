package pw.binom.db.tarantool

import pw.binom.async2
import pw.binom.concurrency.sleep
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.seconds

@OptIn(ExperimentalTime::class)
fun tarantool(func: suspend (TarantoolConnection) -> Unit) {
    val manager = NetworkDispatcher()
    val done = async2 {
        var con: TarantoolConnection
        val now = TimeSource.Monotonic.markNow()
        while (true) {
            try {
                val address = NetworkAddress.Immutable(
                    host = "127.0.0.1",
                    port = 25321,
                )
                con = TarantoolConnection.connect(
                    address = address,
                    manager = manager,
                    userName = "server",
                    password = "server",
                )
                break
            } catch (e: Throwable) {
                if (now.elapsedNow() > 10.seconds) {
                    throw RuntimeException("Connection Timeout")
                }
                sleep(100)
            }
        }
        try {
            func(con)
        } finally {
            con.asyncClose()
        }
    }

    val now = TimeSource.Monotonic.markNow()
    while (!done.isDone) {
        if (now.elapsedNow() > 10.0.seconds) {
            throw RuntimeException("Timeout")
        }
        manager.select(1000)
    }
    if (done.isFailure) {
        throw done.exceptionOrNull!!
    }
}