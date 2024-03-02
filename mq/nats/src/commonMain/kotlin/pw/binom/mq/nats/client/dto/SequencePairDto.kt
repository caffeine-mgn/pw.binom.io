package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SequencePairDto(
  @SerialName("consumer_seq")
  val consumerSeq: Long,
  @SerialName("stream_seq")
  val streamSeq: Long,
)
