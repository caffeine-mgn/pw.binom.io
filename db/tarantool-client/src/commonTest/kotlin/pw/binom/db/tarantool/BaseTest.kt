package pw.binom.db.tarantool

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import pw.binom.io.IOException
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.useAsync
import pw.binom.network.MultiFixedSizeThreadNetworkDispatcher
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

abstract class BaseTest {
//    object TarantoolContainer : TestContainer(
//        image = "tarantool/tarantool:2.6.2",
//        environments = mapOf(
//            "TARANTOOL_USER_NAME" to "server",
//            "TARANTOOL_USER_PASSWORD" to "server",
//        ),
//        ports = listOf(
//            Port(internalPort = 3301)
//        ),
//        reuse = true,
//    )

  @OptIn(ExperimentalTime::class)
  fun tarantool(func: suspend (TarantoolConnectionImpl) -> Unit) =
    runTest(dispatchTimeoutMs = 10 * 1000) {
      val now = TimeSource.Monotonic.markNow()
      val manager = MultiFixedSizeThreadNetworkDispatcher(1)
//        TarantoolContainer {
      delay(1.seconds)
      do {
        println("Connection...")
        val address =
          InetSocketAddress.resolve(
            host = "127.0.0.1",
            port = 7040,
          )
        val connection =
          try {
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
          println("Connected! Try to test")
          connection.useAsync { con ->
            func(con)
          }
        } finally {
          break
        }
      } while (true)
    }
//    }
}
