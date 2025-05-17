package pw.binom.strong.nats.client

import pw.binom.mq.Consumer
import pw.binom.mq.Producer
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.NatsTopic
import pw.binom.mq.nats.NatsTopicImpl
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.inject

abstract class AbstractNatsTopic : NatsTopic {
    private val connection: NatsMqConnection by inject()

    private var internalTopic: NatsTopic? = null

    private fun getInternalTopic() = internalTopic ?: TODO("Topic not created")

    init {
        BeanLifeCycle.postConstruct {
            internalTopic = connection.getOrCreateTopic(subject)
        }

        BeanLifeCycle.preDestroy {
            internalTopic?.asyncCloseAnyway()
        }
    }

    override suspend fun createProducer() =
        getInternalTopic().createProducer()


    override suspend fun clean() {
        getInternalTopic().clean()
    }

    override suspend fun delete() {
        getInternalTopic().delete()
    }

    override suspend fun createConsumer(group: String?, start: Boolean, func: suspend (NatsMessage) -> Unit) =
        getInternalTopic().createConsumer(group = group, start = start, func = func)
}
