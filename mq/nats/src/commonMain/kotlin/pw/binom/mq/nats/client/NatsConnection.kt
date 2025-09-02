package pw.binom.mq.nats.client

import kotlinx.coroutines.channels.ReceiveChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable
import pw.binom.mq.nats.exceptions.NatsConnectionClosedException
import kotlin.coroutines.cancellation.CancellationException

interface NatsConnection : AsyncCloseable {

  suspend fun send(
    subject: String,
    headers: HeadersBody = HeadersBody.empty,
    replyTo: String? = null,
    data: ByteArray? = null,
  )

  @Throws(NatsConnectionClosedException::class, CancellationException::class)
  suspend fun subscribe(
    subject: String,
    group: String? = null,
    forMessages: Int = -1,
  ): ReceiveChannel<NatsMessage>

  @Throws(NatsConnectionClosedException::class, CancellationException::class)
  suspend fun subscribe(
    subject: String,
    group: String? = null,
    forMessages: Int = -1,
    listener: suspend (NatsMessage?) -> Unit,
  ): Closeable

  @Throws(NatsConnectionClosedException::class, CancellationException::class)
  suspend fun sendAndReceive(
    subject: String,
    headers: HeadersBody = HeadersBody.empty,
    data: ByteArray?,
  ): NatsMessage
}
