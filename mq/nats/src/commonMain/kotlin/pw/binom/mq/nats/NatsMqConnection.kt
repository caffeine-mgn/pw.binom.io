package pw.binom.mq.nats

import pw.binom.mq.MqConnection
import pw.binom.mq.nats.client.NatsReader

interface NatsMqConnection : MqConnection {
  val jetStream: JetStreamMqConnection?
  val reader: NatsReader
  override suspend fun createTopic(name: String): NatsTopic
  override suspend fun getTopic(name: String): NatsTopic
  override suspend fun getOrCreateTopic(name: String) = createTopic(name)
}
