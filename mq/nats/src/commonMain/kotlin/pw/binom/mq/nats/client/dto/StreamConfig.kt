package pw.binom.mq.nats.client.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.mq.nats.client.DateTimeRFC3339
import pw.binom.mq.nats.client.DurationNanoSerializer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

@Serializable
data class External(
  @SerialName("api")
  val api: String?,
  @SerialName("deliver")
  val deliver: String?,
)

@Serializable
data class SourceBase(
  @SerialName("name")
  val name: String?,
  @SerialName("opt_start_seq")
  val startSeq: Long,
  @SerialName("opt_start_time")
  @Serializable(DateTimeRFC3339::class)
  val startTime: DateTime?,
  @SerialName("filter_subject")
  val filterSubject: String?,
  @SerialName("external")
  val external: External?,
  @SerialName("subject_transforms")
  val subjectTransforms: List<SubjectTransform>?,
)

enum class RetentionPolicy(val policy: String) {
  @SerialName("limits")
  Limits("limits"),

  @SerialName("interest")
  Interest("interest"),

  @SerialName("workqueue")
  WorkQueue("workqueue"),
}

enum class CompressionOption(val policy: String) {
  @SerialName("none")
  None("none"),

  @SerialName("s2")
  S2("s2"),
}

enum class StorageType(val policy: String) {
  @SerialName("file")
  File("file"),

  @SerialName("memory")
  Memory("memory"),
}

enum class DiscardPolicy(val policy: String) {
  @SerialName("new")
  New("new"),

  @SerialName("old")
  Old("old"),
}

@Serializable
data class ConsumerLimits(
  @Serializable(DurationNanoSerializer::class)
  @SerialName("inactive_threshold")
  val inactiveThreshold: Duration? = null,
  @SerialName("max_ack_pending")
  val maxAckPending: Int? = null,
)

@Serializable
data class SubjectTransform(
  @SerialName("src")
  val source: String,
  @SerialName("dest")
  val destination: String,
)

@Serializable
data class Placement(
  @SerialName("cluster")
  val cluster: String?,
  @SerialName("tags")
  val tags: List<String>?,
)

@Serializable
data class Republish(
  @SerialName("src")
  val source: String?,
  @SerialName("dest")
  val destination: String?,
  @SerialName("headers_only")
  val headersOnly: Boolean,
)

@Serializable
data class StreamConfig(
  // see builder for defaults
  @SerialName("name")
  val name: String,
  @SerialName("description")
  val description: String? = null,
  @SerialName("subjects")
  val subjects: List<String>,
  @SerialName("retention")
  val retentionPolicy: RetentionPolicy = RetentionPolicy.Limits,
  @SerialName("compression")
  val compressionOption: CompressionOption = CompressionOption.None,
  @SerialName("max_consumers")
  val maxConsumers: Long = -1,
  @SerialName("max_msgs")
  val maxMsgs: Long = -1,
  @SerialName("max_msgs_per_subject")
  val maxMsgsPerSubject: Long = -1,
  @SerialName("max_bytes")
  val maxBytes: Long = -1,
  @Serializable(DurationNanoSerializer::class)
  @SerialName("max_age")
  val maxAge: Duration = 365.days,
  @SerialName("max_msg_size")
  val maxMsgSize: Long = -1,
  @SerialName("storage")
  val storageType: StorageType,
  @SerialName("num_replicas")
  val replicas: Int = 1,
  @SerialName("no_ack")
  val noAck: Boolean = false,
  @SerialName("template_owner")
  val templateOwner: String? = null,
  @SerialName("discard")
  val discardPolicy: DiscardPolicy = DiscardPolicy.Old,
  @Serializable(DurationNanoSerializer::class)
  @SerialName("duplicate_window")
  val duplicateWindow: Duration? = null,
  @SerialName("placement")
  val placement: Placement? = null,
  @SerialName("republish")
  val republish: Republish? = null,
  @SerialName("subject_transform")
  val subjectTransform: SubjectTransform? = null,
  @SerialName("consumer_limits")
  val consumerLimits: ConsumerLimits? = null,
  @SerialName("mirror")
  val mirror: SourceBase? = null,
  @SerialName("sources")
  val sources: List<SourceBase> = emptyList(),
  @SerialName("sealed")
  val sealed: Boolean = false,
  @SerialName("allow_rollup_hdrs")
  val allowRollup: Boolean = true,
  @SerialName("allow_direct")
  val allowDirect: Boolean = true,
  @SerialName("mirror_direct")
  val mirrorDirect: Boolean = true,
  @SerialName("deny_delete")
  val denyDelete: Boolean = false,
  @SerialName("deny_purge")
  val denyPurge: Boolean = false,
  @SerialName("discard_new_per_subject")
  val discardNewPerSubject: Boolean = false,
  @SerialName("metadata")
  val metadata: Map<String, String> = emptyMap(),
  @SerialName("first_seq")
  val firstSequence: Long = 1,
) {
  init {
    require(firstSequence >= 1) { "firstSequence should be great or equals 1" }
  }
}
