package pw.binom.mq

import pw.binom.io.AsyncCloseable

interface MqConnection : AsyncCloseable {
  companion object;

  suspend fun createTopic(name: String): Topic<out Message>

  suspend fun getTopic(name: String): Topic<out Message>?

  suspend fun getOrCreateTopic(name: String) = getTopic(name) ?: createTopic(name)
}
