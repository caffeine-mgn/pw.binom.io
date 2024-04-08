package pw.binom.mq.nats

import pw.binom.io.ByteBuffer
import pw.binom.mq.Headers
import pw.binom.mq.Producer

class NatsProducer(val topic: NatsTopic) : Producer {
  override suspend fun send(
    headers: Headers,
    data: ByteArray,
  ) {
    send(
      headers = headers,
      data = data,
      replyTo = null,
    )
  }

  override suspend fun send(
    headers: Headers,
    data: ByteBuffer,
  ) {
    send(
      headers = headers,
      data = data,
      replyTo = null,
    )
  }

  suspend fun send(
    headers: Headers,
    data: ByteArray,
    replyTo: String? = null,
  ) {
    this.topic.connection.reader.connection.publish(
      subject = topic.subject,
      headers = headers.toNatsHeaders(),
      data = data,
      replyTo = replyTo,
    )
  }

  suspend fun send(
    headers: Headers,
    data: ByteBuffer,
    replyTo: String? = null,
  ) {
    this.topic.connection.reader.connection.publish(
      subject = topic.subject,
      headers = headers.toNatsHeaders(),
      data = data,
      replyTo = replyTo,
    )
  }

  override suspend fun asyncClose() {
    // Do nothing
  }
}
