package pw.binom.mq.nats

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.use
import pw.binom.io.useAsync
import pw.binom.mq.MqConnection
import pw.binom.mq.nats.client.*
import pw.binom.network.Network
import pw.binom.network.tcpConnect
import kotlin.coroutines.coroutineContext
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

  suspend fun tcpConnect() = Dispatchers.Network.tcpConnect(InetSocketAddress.resolve("127.0.0.1", NATS_PORT))

  fun mqConnection(func: suspend (NatsMqConnection) -> Unit) =
    testing {
      tcpConnect().use { tcp ->
        MqConnection.nats(
          channel = LoggingAsyncChannel(tcp),
          context = coroutineContext,
        ).useAsync {
          func(it)
        }
      }
    }

  fun jetStream(func: suspend (JetStreamMqConnection) -> Unit) =
    mqConnection {
      func(it.jetStream!!)
    }

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
