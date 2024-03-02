package pw.binom.mq.nats

import pw.binom.io.ByteBuffer
import pw.binom.mq.Headers
import pw.binom.mq.Producer

class NatsProducer(val topic: NatsTopic) : Producer {
  override suspend fun send(
    headers: Headers,
    data: ByteArray,
  ) {
    this.topic.connection.reader.connection.publish(
      subject = topic.subject,
      headers = headers.toNatsHeaders(),
      data = data,
    )
  }

  override suspend fun send(
    headers: Headers,
    data: ByteBuffer,
  ) {
    this.topic.connection.reader.connection.publish(
      subject = topic.subject,
      headers = headers.toNatsHeaders(),
      data = data,
    )
  }

  override suspend fun asyncClose() {
    // Do nothing
  }
}
