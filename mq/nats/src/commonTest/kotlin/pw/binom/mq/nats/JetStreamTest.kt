package pw.binom.mq.nats

import kotlinx.coroutines.delay
import pw.binom.io.useAsync
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class JetStreamTest : BaseTest() {
  @Test
  fun consumerTest() =
    jetStream {
      it.createTopic(Random.nextUuid().toShortString()).useAsync { topic ->
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
