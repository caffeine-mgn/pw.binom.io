package pw.binom.mq

import pw.binom.io.AsyncCloseable

interface MqConnection : AsyncCloseable {
  companion object;

  suspend fun createTopic(name: String): Topic

  suspend fun getTopic(name: String): Topic?
}
