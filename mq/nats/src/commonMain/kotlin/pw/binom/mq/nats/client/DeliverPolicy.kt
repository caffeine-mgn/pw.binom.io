package pw.binom.mq.nats.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DeliverPolicy {
  /**
   * Start receiving from the earliest available message in the stream.
   */
  @SerialName("all")
  All,

  /**
   * Start with the last message added to the stream, or the last message matching the consumer's filter subject if defined.
   */
  @SerialName("last")
  Last,

  /**
   * Start receiving messages created after the consumer was created.
   */
  @SerialName("new")
  New,

  /**
   * Start at the first message with the specified sequence number. The consumer must specify `OptStartSeq` defining the sequence number.
   */
  @SerialName("by_start_sequence")
  ByStartSequence,

  /**
   * Start with messages on or after the specified time. The consumer must specify `OptStartTime` defining the start time.
   */
  @SerialName("by_start_time")
  ByStartTime,

  /**
   * Start with the latest message for each filtered subject currently in the stream.
   */
  @SerialName("last_per_subject")
  LastPerSubject,
}
