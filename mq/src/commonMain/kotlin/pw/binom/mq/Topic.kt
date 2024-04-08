package pw.binom.mq

import pw.binom.io.AsyncCloseable

interface Topic<T:Message> : AsyncCloseable {
  companion object;

  suspend fun createProducer(): Producer

  suspend fun clean()

  suspend fun delete()

  suspend fun createConsumer(
    group: String? = null,
    func: suspend (T) -> Unit,
  ): Consumer
}
