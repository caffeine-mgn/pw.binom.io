package pw.binom.strong.nats.client

import pw.binom.SafeException
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.NatsProducer
import pw.binom.mq.nats.NatsTopicImpl
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.inject

abstract class AbstractNatsProducer {
  private val connection: NatsMqConnection by inject()
  protected abstract val topicName: String

  private var topic: NatsTopicImpl? = null
  private var internalProducer: NatsProducer? = null
  protected val producer: NatsProducer
    get() = internalProducer ?: TODO("Producer not created")


  init {
    BeanLifeCycle.postConstruct {
      SafeException.async {
        val topic = connection.getOrCreateTopic(topicName)
        internalProducer = topic.createProducer()
        this@AbstractNatsProducer.topic = topic
      }
    }
  }
}
