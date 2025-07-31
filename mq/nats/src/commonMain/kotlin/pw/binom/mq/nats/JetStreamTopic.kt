package pw.binom.mq.nats

import pw.binom.mq.Message
import pw.binom.mq.Topic
import pw.binom.mq.nats.client.AckPolicy
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.mq.nats.client.dto.ConsumerInfoResponseDto
import pw.binom.mq.nats.client.dto.ErrorDto
import pw.binom.mq.nats.client.dto.MessageGetRequestDto
import pw.binom.mq.nats.client.dto.StreamConfig

class JetStreamTopic(
  val config: StreamConfig,
  val connection: JetStreamMqConnection,
) : Topic<Message, JetStreamProducer, JetStreamAutoConsumer> {
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

  suspend fun createManualConsumer(
    config: ConsumerConfiguration,
  ): JetStreamManualConsumer {
    val name = config.name ?: config.durableName!!
    if (getConsumerInfo(name) != null) {
      throw IllegalStateException("Consumer $name already exist in stream ${this.config.name}")
    }
    return JetStreamManualConsumer(
      config = config,
      topic = this,
    )
  }

  suspend fun getManualConsumerOrNull(name: String): JetStreamManualConsumer? {
    val exist = getConsumerInfo(name) ?: return null
    return JetStreamManualConsumer(
      config = exist.config!!,
      topic = this,
    )
  }

  suspend fun getOrCreateManualConsumer(config: ConsumerConfiguration): JetStreamManualConsumer {
    val name = config.name ?: config.durableName!!
    val exist = getConsumerInfo(name)
    return if (exist != null) {
      JetStreamManualConsumer(
        config = exist.config!!,
        topic = this,
      )
    } else {
      connection.js.createConsumer(
        streamName = this.config.name,
        config = config,
      )
      JetStreamManualConsumer(
        config = config,
        topic = this,
      )
    }
  }

  suspend fun createConsumer(
    start: Boolean,
    batchSize: Int = 100,
    config: ConsumerConfiguration,
    func: suspend (NatsMessage) -> Unit,
  ): JetStreamAutoConsumer {
    val name = config.name ?: config.durableName!!
    if (getConsumerInfo(name) != null) {
      throw IllegalStateException("Consumer $name already exist in stream ${this.config.name}")
    }
    val consumer =
      connection.js.createConsumer(
        streamName = this.config.name,
        config = config,
      )
    val jsConsumer = JetStreamAutoConsumer(
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

  suspend fun getConsumerInfo(name: String): ConsumerInfoResponseDto? {
    val info: ConsumerInfoResponseDto = connection.js.getConsumerInfo(
      streamName = this.config.name,
      consumerName = name,
    )
    if (info.error?.code == ErrorDto.NOT_FOUND) {
      return null
    }
    return info
  }

  suspend fun getConsumer(
    name: String,
    start: Boolean = true,
    batchSize: Int = 100,
    func: suspend (NatsMessage) -> Unit,
  ): JetStreamAutoConsumer? {
    val exist = getConsumerInfo(name) ?: return null
    val jsConsumer = JetStreamAutoConsumer(
      config = exist.config!!,
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

  suspend fun getOrCreateConsumer(
    start: Boolean,
    batchSize: Int = 100,
    config: ConsumerConfiguration,
    func: suspend (NatsMessage) -> Unit,
  ): JetStreamAutoConsumer {
    val exist = getConsumer(
      name = config.name ?: config.durableName!!,
      start = start,
      batchSize = batchSize,
      func = func,
    )
    if (exist != null) {
      return exist
    }
    return createConsumer(
      start = start,
      batchSize = batchSize,
      config = config,
      func = func,
    )
  }

  override suspend fun asyncClose() {
    // Do nothing
  }
}
