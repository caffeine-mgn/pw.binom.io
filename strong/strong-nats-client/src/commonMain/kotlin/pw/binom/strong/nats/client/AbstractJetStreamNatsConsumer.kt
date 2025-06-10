package pw.binom.strong.nats.client

import pw.binom.mq.nats.JetStreamAutoConsumer
import pw.binom.mq.nats.JetStreamTopic
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.inject

abstract class AbstractJetStreamNatsConsumer {
  private val connection: NatsMqConnection by inject()
  protected abstract val config: NatsJetStreamConsumerProperties

  protected abstract suspend fun consume(message: NatsMessage)

  private var topic: JetStreamTopic? = null
  private var consumer1: JetStreamAutoConsumer? = null

  init {
    BeanLifeCycle.postConstruct {
      val jetStream = connection.jetStream!!
      val existTopic = jetStream.getTopic(config.streamName)
        ?: throw IllegalStateException("Stream ${config.streamName} doesn't exist")
      topic = existTopic
      val existConsumer = existTopic.getConsumer(
        name = config.name,
        start = true,
        batchSize = config.batchSize,
        func = this::consume
      )
      if (existConsumer != null) {
        consumer1 = existConsumer
        return@postConstruct
      }
      if (!config.autoCreate) {
        throw IllegalStateException("Consumer ${config.name} in stream ${config.streamName} doesn't exist")
      }

      val consumerConfiguration = ConsumerConfiguration(
        durableName = if (config.durable) config.name else null,
        name = if (config.durable) null else config.name,
        memStorage = config.memStorage,
        description = config.description,
        ackPolicy = config.ackPolicy,
      )
      consumer1 = existTopic.createConsumer(
        start = true, config = consumerConfiguration,
        batchSize = config.batchSize,
        func = this::consume,
      )
    }
    BeanLifeCycle.preDestroy {
      consumer1?.asyncCloseAnyway()
      topic?.asyncCloseAnyway()
    }
  }
}
