package pw.binom.mq.nats

import pw.binom.mq.MqConnection
import pw.binom.mq.nats.client.JetStreamImpl
import pw.binom.mq.nats.client.NatsReader
import pw.binom.mq.nats.client.dto.ErrorDto
import pw.binom.mq.nats.client.dto.StorageType
import pw.binom.mq.nats.client.dto.StreamConfig

class JetStreamMqConnection(reader: NatsReader) : MqConnection {
  internal val js = JetStreamImpl(reader)

  override suspend fun asyncClose() {
    // do nothing
  }

  suspend fun createTopic(config:StreamConfig): JetStreamTopic {
    val stream =
      js.create(
        config
      )
    if (!stream.didCreate) {
      throw RuntimeException("Topic already exist")
    }
    return JetStreamTopic(
      config = stream.config,
      connection = this,
    )
  }

  override suspend fun createTopic(name: String): JetStreamTopic {
    val stream =
      js.create(
        StreamConfig(
          name = name,
          storageType = StorageType.Memory,
          subjects = listOf(name),
          noAck = false,
        ),
      )
    if (!stream.didCreate) {
      throw RuntimeException("Topic already exist")
    }
    return JetStreamTopic(
      config = stream.config,
      connection = this,
    )
  }

  override suspend fun getTopic(name: String): JetStreamTopic? {
    val resp = js.getStreamInfo(name)
    if (resp.error?.code == ErrorDto.NOT_FOUND) {
      return null
    }
    return JetStreamTopic(
      config = resp.config ?: TODO("Config not exist"),
      connection = this,
    )
  }
}
