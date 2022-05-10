package pw.binom.mq.nats.client

interface NatsMessage {
    val connection: NatsRawConnection
    val subject: String
    val sid: String
    val replyTo: String?
    val data: ByteArray
}
