package pw.binom.mq.nats

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import pw.binom.mq.nats.client.AckPolicy
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.mq.nats.client.dto.PullRequestOptionsDto
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration

class JetStreamManualConsumer(
  val config: ConsumerConfiguration,
  val topic: JetStreamTopic,
) {

  fun flow(
    count: Int,
    maxBytes: Long = -1,
    expires: Duration? = null,
    noWait: Boolean = false,
  ): Flow<NatsMessage> {
    val pullConfig = PullRequestOptionsDto(
      batch = count,
      maxBytes = maxBytes,
      expires = expires,
      noWait = noWait,
    )
    return kotlinx.coroutines.flow.flow<NatsMessage> {
      while (coroutineContext.isActive) {
        topic.connection.js.pullNext(
          streamName = topic.config.name,
          consumerName = config.consumerName,
          config = pullConfig,
          withAckSupport = config.ackPolicy != AckPolicy.NONE,
        ).collect {
          emit(it)
        }
      }
    }
  }

  suspend fun once(
    count: Int,
    maxBytes: Long = -1,
    expires: Duration? = null,
    noWait: Boolean = false,
  ) = topic.connection.js.pullNext(
    streamName = topic.config.name,
    consumerName = config.consumerName,
    config = PullRequestOptionsDto(
      batch = count,
      maxBytes = maxBytes,
      expires = expires,
      noWait = noWait,
    ),
    withAckSupport = config.ackPolicy != AckPolicy.NONE,
  )

  suspend fun delete() {
    topic.connection.js.deleteConsumer(
      streamName = topic.config.name,
      consumerName = config.name!!,
    )
  }
}
