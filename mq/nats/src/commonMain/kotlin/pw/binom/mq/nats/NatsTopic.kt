package pw.binom.mq.nats

import pw.binom.mq.Topic
import pw.binom.mq.nats.client.NatsMessage

class NatsTopic(val connection: NatsMqConnection, val subject: String) : Topic<NatsMessage> {
  private val producer = NatsProducer(this)

  override suspend fun createProducer() = producer

  override suspend fun clean() {
    // Do nothing
  }

  override suspend fun delete() {
    asyncCloseAnyway()
  }

  override suspend fun createConsumer(
    group: String?,
    start: Boolean,
    func: suspend (NatsMessage) -> Unit,
  ): NatsConsumer {
    val consumer = NatsConsumer(
      group = group,
      topic = this,
      incomeListener = func,
    )
    if (start) {
      consumer.start()
    }
    return consumer
  }

  override suspend fun asyncClose() {
    // Do nothing
  }
}
