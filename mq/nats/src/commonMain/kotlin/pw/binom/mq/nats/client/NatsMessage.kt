package pw.binom.mq.nats.client

import pw.binom.mq.Message

interface NatsMessage : Message {
  val subject: String
  val subscribeId: String
  val replyTo: String?
  val data: ByteArray
  override val headers: NatsHeaders
  override val body: ByteArray
    get() = data
  override val topic: String
    get() = subject

  fun clone() =
    object : NatsMessage {
      override val subject: String = this@NatsMessage.subject
      override val subscribeId: String = this@NatsMessage.subscribeId
      override val replyTo: String? = this@NatsMessage.replyTo
      override val data: ByteArray = this@NatsMessage.data
      override val headers: NatsHeaders
        get() = this@NatsMessage.headers.clone()

      override suspend fun ack() {
        this@NatsMessage.ack()
      }

      override fun toString() =
        "Message(subject='$subject', subscribeId='$subscribeId', replyTo=$replyTo, headers=$headers, data=${data.contentToString()})"
    }
}
