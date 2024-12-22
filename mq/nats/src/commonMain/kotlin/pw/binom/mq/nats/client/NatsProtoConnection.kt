package pw.binom.mq.nats.client

import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer
import pw.binom.uuid.nextUuid
import kotlin.random.Random

interface NatsProtoConnection : AsyncCloseable {
  companion object;

  val config: ConnectInfo

  suspend fun subscribe(
    subject: String,
    group: String? = null,
    subscribeId: String,
  )

  suspend fun unsubscribe(
    subscribeId: String,
    afterMessages: Int = 0,
  )

  suspend fun subscribeEx(
    subject: String,
    group: String? = null,
  ): AsyncCloseable {
    val subscribeId = "subscribe-" + Random.nextUuid().toString()
    subscribe(
      subject = subject,
      subscribeId = subscribeId,
      group = group,
    )
    return AsyncCloseable {
      unsubscribe(
        subscribeId = subscribeId,
      )
    }
  }

  suspend fun readMessage(): NatsMessage

  suspend fun publish(
    subject: String,
    replyTo: String? = null,
    headers: HeadersBody = HeadersBody.empty,
    data: ByteArray?,
  )

  suspend fun publish(
    subject: String,
    replyTo: String? = null,
    headers: HeadersBody = HeadersBody.empty,
    data: ByteBuffer?,
  )
}
