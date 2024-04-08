package pw.binom.mq.nats

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.io.AsyncCloseable
import pw.binom.mq.Consumer
import pw.binom.mq.Message
import pw.binom.mq.nats.client.NatsMessage

class NatsConsumer(
  val group: String?,
  val topic: NatsTopic,
  val incomeListener: suspend (NatsMessage) -> Unit,
) : Consumer {
  private var listener: AsyncCloseable? = null
  private val listenerLock = SpinLock()

  override suspend fun start() {
    listenerLock.synchronize {
      if (listener == null) {
        listener =
          topic.connection.reader.subscribe(
            subject = topic.subject,
            group = group,
            listener = incomeListener,
          )
      }
    }
  }

  override suspend fun stop() {
    listenerLock.synchronize {
      val old = listener ?: return
      listener = null
      old
    }.asyncCloseAnyway()
  }

  override suspend fun deleteAndClose() {
    asyncClose()
  }

  override val isReceiving: Boolean
    get() = listenerLock.synchronize { listener != null }

  override suspend fun asyncClose() {
    stop()
  }
}
