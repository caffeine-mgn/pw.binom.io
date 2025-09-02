package pw.binom.strong.nats.client

import pw.binom.SafeException
import pw.binom.mq.nats.JetStreamAutoConsumer
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.NatsTopic
import pw.binom.mq.nats.client.ConsumerConfiguration
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.ServiceProvider
import pw.binom.strong.inject

object Nats {
  fun createConsumer(
    config: Lazy<NatsJetStreamConsumerProperties>,
    closeOnDestroy: Boolean = true,
    func: suspend (NatsMessage) -> Unit,
  ): Lazy<JetStreamAutoConsumer> {
    var consumer1: JetStreamAutoConsumer? = null
    val consumer by config
    val nats by inject<NatsMqConnection>()
    BeanLifeCycle.postConstruct {
      val jetStream = nats.jetStream!!
      val existTopic = jetStream.getTopic(consumer.streamName)
      if (existTopic == null) {
        throw IllegalStateException("Stream ${consumer.streamName} doesn't exist")
      }

      val existConsumer = existTopic.getConsumer(
        name = consumer.name,
        start = true,
        batchSize = consumer.batchSize,
        func = func
      )
      if (existConsumer == null && !consumer.autoCreate) {
        throw IllegalStateException("Consumer ${consumer.name} in stream ${consumer.streamName} doesn't exist")
      }

      val consumerConfiguration = ConsumerConfiguration(
        durableName = if (consumer.durable) consumer.name else null,
        name = if (consumer.durable) null else consumer.name,
        memStorage = consumer.memStorage,
        description = consumer.description,
      )
      consumer1 = existTopic.createConsumer(
        start = true, config = consumerConfiguration,
        batchSize = consumer.batchSize,
        func = func
      )
    }

    if (closeOnDestroy) {
      BeanLifeCycle.postConstruct {
        consumer1?.asyncCloseAnyway()
      }
    }
    return ServiceProvider.provide { consumer1!! }
  }

  fun createProducer(config: Lazy<NatsProducerProperties>, closeOnDestroy: Boolean = true): Lazy<NatsTopic> {
    var topic: NatsTopic? = null
    var producer: pw.binom.mq.nats.NatsProducer? = null
    val config by config
    BeanLifeCycle.postConstruct {
      val nats by inject<NatsMqConnection>()
      val (topic1, producer1) = SafeException.async {
        val topic = nats.getOrCreateTopic(config.subject)
        onException { topic.asyncClose() }
        val producer=topic.createProducer()
        onException { producer.asyncClose() }
        topic to producer
      }
      topic = topic1
      producer = producer1
    }

    if (closeOnDestroy) {
      BeanLifeCycle.preDestroy {
        topic?.asyncClose()
        producer?.asyncClose()
      }
    }
    return ServiceProvider.provide { topic!! }
  }
}
