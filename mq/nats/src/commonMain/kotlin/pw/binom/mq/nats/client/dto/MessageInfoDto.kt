@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.mq.nats.client.dto

import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import pw.binom.date.DateTime
import pw.binom.mq.nats.client.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MessageInfoDto(
  val direct: Boolean,
  val subject: String,
  val seq: Long = 0,
  val data: ByteArray?,
  val time: DateTime? = null,
  val headers: NatsHeaders? = null,
  val stream: String? = null,
  val lastSeq: Long = 0,
) {
  companion object {
    private val noCopyDirectHeaders =
      setOf(
        NatsHeaders.NATS_SUBJECT,
        NatsHeaders.NATS_SEQUENCE,
        NatsHeaders.NATS_TIMESTAMP,
        NatsHeaders.NATS_STREAM,
        NatsHeaders.NATS_LAST_SEQUENCE,
      )

    @OptIn(ExperimentalEncodingApi::class)
    fun create(
      msg: NatsMessage,
      direct: Boolean,
      streamName: String,
      onError: (msg: String) -> Nothing,
    ): MessageInfoDto {
      return if (direct) {
        val msgHeaders = msg.headers
        MessageInfoDto(
          subject = msgHeaders.getLast(NatsHeaders.NATS_SUBJECT)!!,
          data = msg.data,
          seq = msgHeaders.getFirst(NatsHeaders.NATS_SEQUENCE)?.toLongOrNull() ?: 0,
          time = msgHeaders.getFirst(NatsHeaders.NATS_TIMESTAMP)?.let { DateTimeRFC3339.decode(it) },
          stream = msgHeaders.getFirst(NatsHeaders.NATS_STREAM) ?: streamName,
          lastSeq = msgHeaders.getFirst(NatsHeaders.NATS_LAST_SEQUENCE)?.toLongOrNull() ?: -1,
          headers =
            msgHeaders.filter { key, _ ->
              key !in noCopyDirectHeaders
            },
          direct = true,
        )
      } else {
        val body = JetStreamApiJsonUtils.checkError(msg.data, onError).jsonObject
        val mjv = body["message"]!!.jsonObject
        MessageInfoDto(
          subject = mjv["subject"]!!.jsonPrimitive.content,
          data = mjv["data"]?.jsonPrimitive?.content?.let { Base64.decode(it) },
          seq = mjv["seq"]?.jsonPrimitive?.long ?: 0L,
          time = mjv["time"]!!.jsonPrimitive.content.let { DateTimeRFC3339.decode(it) },
          headers =
            mjv["hdrs"]?.jsonPrimitive?.content?.let { Base64.decode(it) }?.let { HeadersBody(it).parse() }
              ?: NatsHeaders.empty,
          stream = streamName,
          direct = false,
          lastSeq = -1,
        )
      }
    }
  }
}
