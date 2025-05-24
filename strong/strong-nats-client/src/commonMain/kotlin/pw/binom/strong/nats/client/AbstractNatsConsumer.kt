package pw.binom.strong.nats.client

import pw.binom.SafeException
import pw.binom.mq.nats.NatsConsumer
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.NatsTopicImpl
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.inject

abstract class AbstractNatsConsumer {
  private val connection: NatsMqConnection by inject()
  protected abstract val consumerConfig:NatsConsumerProperties
  private var topic: NatsTopicImpl? = null
  private var consumer: NatsConsumer? = null

  protected abstract suspend fun income(message: NatsMessage)

  init {
    BeanLifeCycle.postConstruct {
      SafeException.async {
        val topic = connection.getOrCreateTopic(consumerConfig.topic).closeOnException()
        this@AbstractNatsConsumer.consumer = topic.createConsumer(group = consumerConfig.group) { message ->
          income(message)
        }.closeOnException()
        this@AbstractNatsConsumer.topic = topic
      }
    }

    BeanLifeCycle.preDestroy {
      consumer?.asyncCloseAnyway()
    }

  }
}
