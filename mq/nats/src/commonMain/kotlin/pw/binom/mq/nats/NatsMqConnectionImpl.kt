@file:OptIn(DelicateCoroutinesApi::class)

package pw.binom.mq.nats

import kotlinx.coroutines.DelicateCoroutinesApi
import pw.binom.mq.nats.client.NatsReader

internal class NatsMqConnectionImpl(override val reader: NatsReader) : NatsMqConnection {
  override val jetStream: JetStreamMqConnection? by lazy {
    if (reader.config.jetStreamEnabled) {
      JetStreamMqConnection(reader)
    } else {
      null
    }
  }

  override suspend fun createTopic(name: String) = NatsTopic(connection = this, subject = name)

  override suspend fun getTopic(name: String) = createTopic(name)

  override suspend fun asyncClose() {
    reader.asyncClose()
  }
}
