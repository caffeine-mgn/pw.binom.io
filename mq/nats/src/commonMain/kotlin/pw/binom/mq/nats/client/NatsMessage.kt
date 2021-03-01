package pw.binom.mq.nats.client

import pw.binom.UUID

interface NatsMessage {
    val connection: NatsRawConnection
    val subject: String
    val sid: UUID
    val replyTo: String?
    val data: ByteArray
}