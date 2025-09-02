package pw.binom.strong.nats.client

import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.mq.nats.client.ReconnactableConnect
import pw.binom.mq.nats.jetstream.createConsumer
import pw.binom.mq.nats.jetstream.getStreamConsumer
import pw.binom.network.NetworkManager
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.inject

abstract class AbstractJetStreamNatsConsumer {
  private val connection: ReconnactableConnect by inject()
  protected abstract val config: NatsJetStreamConsumerProperties
  private val networkManager: NetworkManager by inject()

  protected abstract suspend fun consume(message: NatsMessage)

  private var job: Job? = null

  init {
    BeanLifeCycle.postConstruct {
      val streamConfig = ConsumerConfiguration(
        durableName = if (config.durable) config.name else null,
        name = if (config.durable) null else config.name,
        memStorage = config.memStorage,
        description = config.description,
        ackPolicy = config.ackPolicy,
      )
      var exist = connection.getStreamConsumer(
        streamName = config.streamName,
        consumerName = streamConfig.consumerName,
      )
      if (exist == null) {
        if (!config.autoCreate) {
          throw IllegalStateException("Consumer ${streamConfig.consumerName} in stream ${config.streamName} doesn't exist")
        } else {
          exist = connection.createConsumer(
            streamName = config.streamName,
            config = streamConfig,
          )
        }
      }
      job = networkManager.launch {
        while (isActive) {
          consume(exist.pull(batch = 1).receive())
        }
      }
    }
    BeanLifeCycle.preDestroy {
      job?.cancel()
    }
  }
}
