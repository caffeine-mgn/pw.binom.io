package pw.binom.mq.nats

import pw.binom.io.ByteBuffer
import pw.binom.mq.*
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.mq.nats.client.NatsReader

interface NatsMqConnection : MqConnection {
  val jetStream: JetStreamMqConnection?
  val reader: NatsReader
  override suspend fun createTopic(name: String): NatsTopicImpl
  override suspend fun getTopic(name: String): NatsTopicImpl
  override suspend fun getOrCreateTopic(name: String) = createTopic(name)
  suspend fun sendAndReceive(topic: String, func: suspend (Producer<Headers>) -> Unit): NatsMessage =
    reader.sendAndReceive { connection, responseSubject ->
      val producer = object : Producer<Headers> {
        override suspend fun send(headers: Headers, data: ByteArray) {
          connection.publish(subject = topic, replyTo = responseSubject, headers = headers.toNatsHeaders(), data = data)
        }

        override suspend fun send(headers: Headers, data: ByteBuffer) {
          connection.publish(subject = topic, replyTo = responseSubject, headers = headers.toNatsHeaders(), data = data)
        }

        override suspend fun asyncClose() {
        }
      }
      func(producer)
    }
}
