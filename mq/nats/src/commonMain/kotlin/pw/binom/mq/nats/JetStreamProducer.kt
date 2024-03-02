package pw.binom.mq.nats

import pw.binom.io.ByteBuffer
import pw.binom.mq.Headers
import pw.binom.mq.Producer

class JetStreamProducer(val subject: String, val topic: JetStreamTopic) : Producer {
  override suspend fun send(
    headers: Headers,
    data: ByteArray,
  ) {
    this.topic.connection.js.publish(
      subject = subject,
      headers = headers.toNatsHeaders(),
      data = data,
    )
  }

  override suspend fun send(
    headers: Headers,
    data: ByteBuffer,
  ) {
    this.topic.connection.js.publish(
      subject = subject,
      headers = headers.toNatsHeaders(),
      body = data,
    )
  }

  override suspend fun asyncClose() {
    // Do nothing
  }
}
