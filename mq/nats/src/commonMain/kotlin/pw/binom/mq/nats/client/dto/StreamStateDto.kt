package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.mq.nats.client.DateTimeRFC3339

@Serializable
data class StreamStateDto(
  @SerialName("messages")
  val messages: Long,
  @SerialName("bytes")
  val bytes: Long,
  @SerialName("first_seq")
  val firstSeq: Long,
  @SerialName("last_seq")
  val lastSeq: Long,
  @SerialName("consumer_count")
  val consumerCount: Int,
  @SerialName("first_ts")
  @Serializable(DateTimeRFC3339::class)
  val firstTs: DateTime,
  @SerialName("last_ts")
  @Serializable(DateTimeRFC3339::class)
  val lastTs: DateTime,
  @SerialName("num_subjects")
  val num_subjects: Long = 0,
  @SerialName("num_deleted")
  val num_deleted: Long = 0,
  @SerialName("subjects")
  val subjects: Map<String, Long>? = null,
  @SerialName("deleted")
  val deleted: List<Long>? = null,
  @SerialName("lost")
  @Serializable(DateTimeRFC3339::class)
  val lost: DateTime? = null,
)
