package pw.binom.mq.nats.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer
import pw.binom.uuid.nextUuid
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

interface NatsProtoConnection : AsyncCloseable {
  companion object;

  val config: ConnectInfo

  suspend fun subscribe(
    subject: String,
    group: String? = null,
    subscribeId: String,
  )

  suspend fun subscribe(
    subject: String,
    group: String? = null,
    subscribeId: String,
    forMessages: Int,
  ) {
    require(forMessages > 0) { "Argument \"forMessages\" should be more than zero" }
    subscribe(
      subject = subject,
      group = group,
      subscribeId = subscribeId,
    )
    unsubscribe(
      subscribeId = subscribeId,
      afterMessages = forMessages,
    )
  }

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

  fun messageFlow(): Flow<NatsMessage> = flow {
    while (coroutineContext.isActive) {
      emit(readMessage())
    }
  }

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
