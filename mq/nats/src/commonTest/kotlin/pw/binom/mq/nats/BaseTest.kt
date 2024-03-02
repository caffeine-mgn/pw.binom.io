package pw.binom.mq.nats

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.mq.nats.client.*
import pw.binom.network.Network
import pw.binom.network.tcpConnect
import kotlin.time.Duration.Companion.minutes

abstract class BaseTest {
  companion object {
    const val NATS_PORT = 8122
  }

  fun testing(func: suspend () -> Unit) =
    runTest(timeout = 1.minutes) {
      withContext(Dispatchers.Network) {
        func()
      }
    }

  suspend fun tcpConnect() = Dispatchers.Network.tcpConnect(InetNetworkAddress.create("127.0.0.1", NATS_PORT))

  suspend fun natsConnect() =
    InternalNatsConnection.connect(
      channel = LoggingAsyncChannel(tcpConnect()),
    )

  suspend fun natsReader(incomeMessage: suspend (nats: InternalNatsConnection, message: NatsMessage) -> Unit): NatsReader {
    val con = natsConnect()
    return NatsReader.start(
      con = con,
      incomeListener = { msg ->
        incomeMessage(con, msg)
      },
    )
  }
}
