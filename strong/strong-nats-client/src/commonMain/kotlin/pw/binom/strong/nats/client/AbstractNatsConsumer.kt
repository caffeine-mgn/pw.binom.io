package pw.binom.strong.nats.client

import pw.binom.io.Closeable
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.mq.nats.client.ReconnactableConnect
import pw.binom.strong.BeanLifeCycle
import pw.binom.strong.inject

abstract class AbstractNatsConsumer {
  private val connection: ReconnactableConnect by inject()
  protected abstract val consumerConfig: NatsConsumerProperties

  protected abstract suspend fun income(message: NatsMessage)
  private var listener: Closeable? = null

  init {
    BeanLifeCycle.postConstruct {
      connection.onConnect {
        listener = it.subscribe(
          subject = consumerConfig.topic,
          group = consumerConfig.group,
        ) { msg ->
          if (msg != null) {
            income(msg)
          }
        }
      }
    }

    BeanLifeCycle.preDestroy {
      listener?.close()
    }
  }
}
