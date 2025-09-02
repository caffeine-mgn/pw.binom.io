package pw.binom.mq.nats

import kotlinx.coroutines.channels.ReceiveChannel
import pw.binom.io.ByteBuffer
import pw.binom.mq.*
import pw.binom.mq.nats.client.HeadersBody
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.mq.nats.client.NatsReader

interface NatsMqConnection : MqConnection {
  val jetStream: JetStreamMqConnection?
  val reader: NatsReader
  override suspend fun createTopic(name: String): NatsTopicImpl
  override suspend fun getTopic(name: String): NatsTopicImpl
  override suspend fun getOrCreateTopic(name: String) = createTopic(name)

  suspend fun listen(
    subject: String,
    group: String?,
    messageCount: Int = 0,
  ): ReceiveChannel<NatsMessage>

  suspend fun send(
    subject: String,
    headers: Headers = Headers.empty,
    data: ByteArray? = null,
    replyTo: String? = null,
  ) {
    reader.connection.publish(
      subject = subject,
      replyTo = replyTo,
      headers = headers.toNatsHeaders(),
      data = data
    )
  }

  suspend fun sendAndReceive(
    subject: String,
    headers: Headers,
    data: ByteArray?,
  ): NatsMessage =
    reader.sendAndReceive { connection, replyTo ->
      connection.publish(subject = subject, replyTo = replyTo, headers = headers.toNatsHeaders(), data = data)
    }

  suspend fun sendAndReceive(topic: String, func: suspend (Producer<Headers>) -> Unit): NatsMessage =
    reader.sendAndReceive { connection, responseSubject ->
      val producer = object : Producer<Headers> {
        override suspend fun send(headers: Headers, data: ByteArray) {
          connection.publish(subject = topic, replyTo = responseSubject, headers = headers.toNatsHeaders(), data = data)
        }
        /*
        override suspend fun send(headers: Headers, data: ByteBuffer) {
          connection.publish(subject = topic, replyTo = responseSubject, headers = headers.toNatsHeaders(), data = data)
        }
        */
        override suspend fun asyncClose() {
        }
      }
      func(producer)
    }
}
