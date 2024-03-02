package pw.binom.mq.nats

import pw.binom.mq.Message
import pw.binom.mq.Topic

class NatsTopic(val connection: NatsMqConnection, val subject: String) : Topic {
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
    func: suspend (Message) -> Unit,
  ) = NatsConsumer(
    group = group,
    topic = this,
    incomeListener = func,
  )

  override suspend fun asyncClose() {
    // Do nothing
  }
}
