@file:OptIn(DelicateCoroutinesApi::class)

package pw.binom.mq.nats

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import pw.binom.io.AsyncCloseable
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.mq.nats.client.NatsReader
import kotlin.coroutines.cancellation.CancellationException

internal class NatsMqConnectionImpl(override val reader: NatsReader) : NatsMqConnection {
  override val jetStream: JetStreamMqConnection? by lazy {
    if (reader.config.jetStreamEnabled) {
      JetStreamMqConnection(reader)
    } else {
      null
    }
  }

  override suspend fun createTopic(name: String) = NatsTopicImpl(connection = this, subject = name)

  override suspend fun getTopic(name: String) = createTopic(name)
  override suspend fun listen(
    subject: String,
    group: String?,
    messageCount: Int,
  ): ReceiveChannel<NatsMessage> {
    val channel = Channel<NatsMessage>()
    var subscribe: AsyncCloseable? = null
    subscribe = reader.subscribe(subject = subject, group = group, messageCount = messageCount) { msg ->
      try {
        channel.send(msg)
      } catch (e: CancellationException) {
        subscribe?.asyncClose()
      }
    }
    channel.invokeOnClose {
      reader.scope.launch(reader.context) {
        subscribe.asyncClose()
      }
    }
    return channel
  }

  override suspend fun asyncClose() {
    reader.asyncClose()
  }
}
