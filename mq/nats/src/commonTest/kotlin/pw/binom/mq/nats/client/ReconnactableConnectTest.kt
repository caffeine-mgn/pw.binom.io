package pw.binom.mq.nats.client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class ReconnactableConnectTest : BaseTest() {

  private inner class Connection : ReconnactableConnect.Address {
    private var tcp: TcpConnection? = null
    override suspend fun connect(): NatsProtoConnection {
      require(tcp == null)
      tcp = tcpConnect()
      return InternalNatsConnection.connect(
        channel = tcp!!,
      )
    }

    fun disconnect() {
      tcp?.close()
      tcp = null
    }
  }

  private var streamName = Random.nextUuid().toShortString()
  private var consumerName = Random.nextUuid().toShortString()

  @AfterTest
  fun regenerateStreamName() {
    streamName = Random.nextUuid().toShortString()
    consumerName = Random.nextUuid().toShortString()
  }

  @Test
  fun reconnectTest() = testing {
    val tcpConnection = Connection()
    val mq = ReconnactableConnectImpl(addresses = setOf(tcpConnection))
    mq.waitConnected()
    tcpConnection.disconnect()
    mq.waitNotConnected()
    mq.waitConnected()
    mq.asyncClose()
    mq.waitNotConnected()
  }

  @Test
  fun subscriptionFlowReconnectTest() = testing {
    val tcpConnection = Connection()
    var counter = 0
    val data = Random.nextBytes(100)

    ReconnactableConnectImpl(
      addresses = setOf(tcpConnection)
    ).useAsync { connection ->
      connection.waitConnected()
      val e = connection.subscribe(
        subject = streamName,
      )
      connection.send(subject = streamName, data = data)
      e.receive()
      tcpConnection.disconnect()
      connection.waitNotConnected()
      connection.waitConnected()
      connection.send(subject = streamName, data = data)
      e.receive()
      println("OK!")
    }
  }

  @Test
  fun subscriptionListenerReconnectTest() = testing {
    val tcpConnection = Connection()
    var counter = 0
    val data = Random.nextBytes(100)
    ReconnactableConnectImpl(
      addresses = setOf(tcpConnection)
    ).useAsync { connection ->
      connection.onConnect {
        it.subscribe(subject = streamName) { msg ->
          if (msg != null) {
            counter++
          }
        }
      }
      delay(2.seconds)
      connection.send(subject = streamName, data = data)
      delay(2.seconds)
      tcpConnection.disconnect()
      connection.waitNotConnected()
      delay(2.seconds)
      connection.waitConnected()
      connection.send(subject = streamName, data = data)
      delay(2.seconds)
      assertEquals(2, counter)
    }
  }

  @Test
  fun jsPullReconnectTest() = testing {
    val tcpConnection = Connection()
    val data = Random.nextBytes(100)
    ReconnactableConnectImpl(
      addresses = setOf(tcpConnection)
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
      tcpConnection.disconnect()
      connection.waitNotConnected()
      delay(2.seconds)
      connection.waitConnected()
      GlobalScope.launch {
        delay(2.seconds)
        connection.send(subject = streamName, data = data)
      }
      val msg1 = channel.receive()
      assertContentEquals(data, msg1.data)
      delay(5.seconds)
    }
  }
}
