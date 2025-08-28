package pw.binom.mq.nats

import pw.binom.mq.Headers
import pw.binom.mq.Producer
import pw.binom.mq.nats.client.NatsConnection

class NatsProducer(val connection: NatsConnection,val subject:String) : Producer<Headers> {
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

//  override suspend fun send(
//    headers: Headers,
//    data: ByteBuffer,
//  ) {
//    send(
//      headers = headers,
//      data = data,
//      replyTo = null,
//    )
//  }

  suspend fun send(
    headers: Headers = Headers.empty,
    data: ByteArray,
    replyTo: String? = null,
  ) {
    connection.send(
      subject = subject,
      headers=headers.toNatsHeaders(),
      replyTo = replyTo,
      data = data,
    )
  }

//  suspend fun send(
//    headers: Headers = Headers.empty,
//    data: ByteBuffer,
//    replyTo: String? = null,
//  ) {
//    this.topic.connection.reader.connection.publish(
//      subject = topic.subject,
//      headers = headers.toNatsHeaders(),
//      data = data,
//      replyTo = replyTo,
//    )
//  }

  override suspend fun asyncClose() {
    // Do nothing
  }
}
