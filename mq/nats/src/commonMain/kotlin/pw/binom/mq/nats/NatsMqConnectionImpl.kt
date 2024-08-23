@file:OptIn(DelicateCoroutinesApi::class)
package pw.binom.mq.nats

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncChannel
import pw.binom.mq.MqConnection
import pw.binom.mq.nats.client.Auth
import pw.binom.mq.nats.client.InternalNatsConnection
import pw.binom.mq.nats.client.NatsReader
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

internal class NatsMqConnectionImpl(override val reader: NatsReader) : NatsMqConnection {
  override val jetStream: JetStreamMqConnection? =
    if (reader.config.jetStreamEnabled) {
      JetStreamMqConnection(reader)
    } else {
      null
    }

  override suspend fun createTopic(name: String) = NatsTopic(connection = this, subject = name)

  override suspend fun getTopic(name: String) = createTopic(name)

  override suspend fun asyncClose() {
    reader.asyncClose()
  }
}

suspend fun MqConnection.Companion.nats(
  channel: AsyncChannel,
  clientName: String? = null,
  lang: String = "kotlin",
  echo: Boolean = true,
  tlsRequired: Boolean = false,
  version: String = "0.1.x",
  headers: Boolean = true,
  auth: Auth? = null,
  readBufferSize: Int = DEFAULT_BUFFER_SIZE,
  writeBufferSize: Int = DEFAULT_BUFFER_SIZE,
  scope: CoroutineScope = GlobalScope,
  context: CoroutineContext = DefaultEmptyCoroutineContext,
): NatsMqConnection {
  val connection =
    InternalNatsConnection.connect(
      channel = channel,
      clientName = clientName,
      lang = lang,
      echo = echo,
      tlsRequired = tlsRequired,
      version = version,
      headers = headers,
      auth = auth,
      readBufferSize = readBufferSize,
      writeBufferSize = writeBufferSize,
    )
  val reader =
    NatsReader.start(
      con = connection,
      scope = scope,
      context = if (context === DefaultEmptyCoroutineContext) coroutineContext else context,
    )
  return NatsMqConnectionImpl(reader)
}
