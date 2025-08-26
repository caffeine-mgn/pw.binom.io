package pw.binom.mq.nats.client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.io.useAsync
import pw.binom.mq.nats.BaseTest
import pw.binom.mq.nats.createConsumer
import pw.binom.mq.nats.createStream
import pw.binom.mq.nats.jetstream.JetStreamConsumer
import pw.binom.mq.nats.waitConnected
import pw.binom.mq.nats.waitNotConnected
import pw.binom.network.TcpConnection
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.time.Duration.Companion.seconds

class ReconnactableConnectTest : BaseTest() {

  @Test
  fun reconnectTest() = testing {
    var connection: TcpConnection? = null
    val mq = ReconnactableConnect(
      connect = {
        connection = tcpConnect()
        InternalNatsConnection.connect(
          channel = connection,
        )
      },
      addresses = setOf(
        ReconnactableConnect.Address(address = DomainSocketAddress("127.0.0.1", NATS_PORT), auth = null)
      )
    )
    mq.waitConnected()
    connection!!.close()
    delay(7.seconds)
    mq.waitConnected()
    mq.asyncClose()
    mq.waitNotConnected()
  }

  @Test
  fun subscriptionReconnectTest() = testing {
    val streamName = Random.nextUuid().toShortString()
    var tcpConnection: TcpConnection? = null
    var counter = 0
    ReconnactableConnect(
      connect = {
        tcpConnection = tcpConnect()
        InternalNatsConnection.connect(
          channel = tcpConnection,
        )
      },
      addresses = setOf(
        ReconnactableConnect.Address(address = DomainSocketAddress("127.0.0.1", NATS_PORT), auth = null)
      )
    ).useAsync { connection ->
      connection.onConnect {
        it.subscribe(subject = streamName) {
          println("INCOME----->$it")
          counter++
        }
        it.send(subject = streamName, data = byteArrayOf(1))
      }
      delay(7.seconds)
    }
  }

  @Test
  fun jsPullReconnectTest() = testing {
    var tcpConnection: TcpConnection? = null
    val streamName = Random.nextUuid().toShortString()
    val consumerName = Random.nextUuid().toShortString()
    val data = Random.nextBytes(100)
    ReconnactableConnect(
      connect = {
        tcpConnection = tcpConnect()
        InternalNatsConnection.connect(
          channel = tcpConnection,
        )
      },
      addresses = setOf(
        ReconnactableConnect.Address(address = DomainSocketAddress("127.0.0.1", NATS_PORT), auth = null)
      )
    ).useAsync { connection ->
      connection.createStream(streamName)
      connection.createConsumer(streamName = streamName, name = consumerName)
      val channel = JetStreamConsumer.pull(
        connection = connection,
        streamName = streamName,
        consumerName = consumerName,
        expires = 5.seconds,
        batch = 1,
      )
      tcpConnection?.closeAnyway()
      connection.waitNotConnected()
      delay(2.seconds)
      connection.waitConnected()
      GlobalScope.launch {
        delay(2.seconds)
        connection.onConnect {
          it.send(subject = streamName, data = data)
        }
      }
      val msg1 = channel.receive()
      assertContentEquals(data, msg1.data)
    }
  }
}
