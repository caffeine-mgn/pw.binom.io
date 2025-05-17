package pw.binom.mq

import pw.binom.io.AsyncCloseable

interface Topic<T : Message, PRODUCER : Producer<*>, CONSUMER:Consumer> : AsyncCloseable {
  companion object;

  suspend fun createProducer(): PRODUCER

  suspend fun clean()

  suspend fun delete()

  suspend fun createConsumer(
    group: String? = null,
    start: Boolean = true,
    func: suspend (T) -> Unit,
  ): CONSUMER
}
