package pw.binom.mq.nats

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
) : Topic {
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

  override suspend fun createConsumer(
    group: String?,
    func: suspend (Message) -> Unit,
  ): JetStreamConsumer {
    val name = "consumer-" + Random.nextUuid().toShortString()
    val consumer =
      connection.js.createConsumer(
        streamName = config.name,
        config =
          ConsumerConfiguration(
            durableName = name,
            name = name,
            deliverGroup = group,
            ackPolicy = AckPolicy.ALL,
          ),
      )
    return JetStreamConsumer(
      config = consumer.config,
      topic = this,
      incomeListener = func,
      batchSize = 100,
    )
  }

  override suspend fun asyncClose() {
    // Do nothing
  }
}
