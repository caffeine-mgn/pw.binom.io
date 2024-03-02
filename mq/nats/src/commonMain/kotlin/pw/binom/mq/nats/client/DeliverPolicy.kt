package pw.binom.mq.nats.client

import kotlinx.serialization.SerialName

enum class DeliverPolicy {
  @SerialName("all")
  All,

  @SerialName("last")
  Last,

  @SerialName("new")
  New,

  @SerialName("by_start_sequence")
  ByStartSequence,

  @SerialName("by_start_time")
  ByStartTime,

  @SerialName("last_per_subject")
  LastPerSubject,
}
