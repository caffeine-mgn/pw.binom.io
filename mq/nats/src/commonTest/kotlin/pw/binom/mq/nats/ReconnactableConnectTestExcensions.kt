package pw.binom.mq.nats

import kotlinx.coroutines.delay
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.JetStreamApi
import pw.binom.mq.nats.client.ReconnactableConnect
import pw.binom.mq.nats.client.dto.StorageType
import pw.binom.mq.nats.client.dto.StreamConfig
import kotlin.time.Duration.Companion.seconds

suspend fun ReconnactableConnect.createStream(name: String) =
  onConnect {
    JetStreamApi.createStream(
      config = StreamConfig(
        name = name,
        subjects = listOf(name),
        storageType = StorageType.Memory,
      ), client = it
    )
  }

suspend fun ReconnactableConnect.createConsumer(streamName: String, name: String) =
  onConnect {
    JetStreamApi.createConsumer(
      client = it,
      streamName = streamName,
      config = ConsumerConfiguration(name = name)
    )
  }

suspend fun ReconnactableConnect.waitConnected() {
  while (true) {
    if (isConnected) {
      break
    }
    delay(1.seconds)
  }
}

suspend fun ReconnactableConnect.waitNotConnected() {
  while (true) {
    if (!isConnected) {
      break
    }
    delay(1.seconds)
  }
}
