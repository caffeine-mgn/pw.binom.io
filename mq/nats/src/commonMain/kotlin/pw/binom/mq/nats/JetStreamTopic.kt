package pw.binom.mq.nats

import kotlinx.serialization.EncodeDefault
import pw.binom.mq.Consumer
import pw.binom.mq.Message
import pw.binom.mq.Topic
import pw.binom.mq.nats.client.AckPolicy
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.dto.MessageGetRequestDto
import pw.binom.mq.nats.client.dto.StreamConfig
import pw.binom.uuid.nextUuid
import kotlin.random.Random

class JetStreamTopic(
  val config: StreamConfig,
  val connection: JetStreamMqConnection,
) : Topic<Message> {
  override suspend fun createProducer() =
    JetStreamProducer(
      subject = config.subjects.first(),
      topic = this,
    )

  suspend fun getMessageBySequence(sequence: Long) =
    connection.js.getMessage(
      streamName = config.name,
      config = MessageGetRequestDto.forSequence(sequence),
    )

  override suspend fun clean() {
    connection.js.purgeStream(
      streamName = config.name,
    )
  }

  override suspend fun delete() {
    asyncCloseAnyway()
    connection.js.deleteStream(
      name = config.name,
    )
  }

  suspend fun createConsumer(
    start: Boolean,
    batchSize: Int = 100,
    config: ConsumerConfiguration,
    func: suspend (Message) -> Unit,
  ): JetStreamConsumer {
    val consumer =
      connection.js.createConsumer(
        streamName = this.config.name,
        config = config,
      )
    val jsConsumer = JetStreamConsumer(
      config = consumer.config,
      topic = this,
      incomeListener = func,
      batchSize = batchSize,
    )
    if (start) {
      jsConsumer.start()
    }
    return jsConsumer
  }

  override suspend fun createConsumer(group: String?, start: Boolean, func: suspend (Message) -> Unit) =
    createConsumer(
      start = start,
      func = func,
      config = ConsumerConfiguration(
        deliverGroup = group,
        durableName = group,
        name = group,
        ackPolicy = AckPolicy.ALL,
      ),
      batchSize = 100,
    )

  override suspend fun asyncClose() {
    // Do nothing
  }
}
