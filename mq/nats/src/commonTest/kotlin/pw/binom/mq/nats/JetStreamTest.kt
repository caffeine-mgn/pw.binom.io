package pw.binom.mq.nats

import kotlinx.coroutines.delay
import pw.binom.io.use
import pw.binom.io.useAsync
import pw.binom.mq.MqConnection
import pw.binom.mq.nats.client.LoggingAsyncChannel
import kotlin.coroutines.coroutineContext
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class JetStreamTest : BaseTest() {
  fun mqConnection(func: suspend (NatsMqConnectionImpl) -> Unit) =
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

  @Test
  fun consumerTest() =
    jetStream {
      it.createTopic("test").useAsync { topic ->
        val producer = topic.createProducer()

        (0..9).forEach {
          producer.send(data = "Hello-$it".encodeToByteArray())
        }

        val consumer =
          topic.createConsumer {
            println("message->$it")
            it.ack()
          }
        consumer.start()
        delay(5.seconds)
        (10..19).forEach {
          producer.send(data = "Hello-$it".encodeToByteArray())
        }
        delay(5.seconds)
      }
    }
}
