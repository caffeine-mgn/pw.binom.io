package pw.binom.mq

import pw.binom.io.AsyncCloseable

interface MqConnection : AsyncCloseable {
  companion object;

  suspend fun createTopic(name: String): Topic<out Message, out Producer<*>, out Consumer>

  suspend fun getTopic(name: String): Topic<out Message, out Producer<*>, out Consumer>?

  suspend fun getOrCreateTopic(name: String) = getTopic(name) ?: createTopic(name)
}
