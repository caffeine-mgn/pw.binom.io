package pw.binom.mq.nats.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import kotlin.time.Duration

@Serializable
data class ConsumerConfiguration(
  @SerialName("deliver_policy")
  val deliverPolicy: DeliverPolicy = DeliverPolicy.All,
  @SerialName("ack_policy")
  val ackPolicy: AckPolicy = AckPolicy.NONE,
  @SerialName("replay_policy")
  val replayPolicy: ReplayPolicy = ReplayPolicy.INSTANT,
  @SerialName("description")
  val description: String? = null,
  @SerialName("durable_name")
  val durableName: String? = null,
  @SerialName("name")
  val name: String? = null,
  @SerialName("deliver_subject")
  val deliverSubject: String? = null,
  @SerialName("deliver_group")
  val deliverGroup: String? = null,
  @SerialName("sample_freq")
  val sampleFreq: String? = null,
  @SerialName("opt_start_time")
  @Serializable(DateTimeRFC3339::class)
  val optStartTime: DateTime? = null,
  @SerialName("ack_wait")
  @Serializable(DurationNanoSerializer::class)
  val ackWait: Duration? = null,
  @SerialName("idle_heartbeat")
  @Serializable(DurationNanoSerializer::class)
  val idleHeartbeat: Duration? = null,
  @SerialName("max_expires")
  @Serializable(DurationNanoSerializer::class)
  val maxExpires: Duration? = null,
  @SerialName("inactive_threshold")
  @Serializable(DurationNanoSerializer::class)
  val inactiveThreshold: Duration? = null,
  @SerialName("opt_start_seq")
  val optStartSeq: Long? = null,
  @SerialName("max_deliver")
  val maxDeliver: Int? = null,
  @SerialName("rate_limit_bps")
  val rateLimitBps: Long? = null,
  @SerialName("max_ack_pending")
  val maxAckPending: Int? = null,
  @SerialName("max_waiting")
  val maxPullWaiting: Int? = null,
  @SerialName("max_batch")
  val maxBatch: Int? = null,
  @SerialName("max_bytes")
  val maxBytes: Int? = null,
  @SerialName("num_replicas")
  val numReplicas: Int? = null,
  @SerialName("flow_control")
  val flowControl: Boolean? = null,
  @SerialName("headers_only")
  val headersOnly: Boolean? = null,
  @SerialName("mem_storage")
  val memStorage: Boolean? = null,
  @SerialName("backoff")
  val backoff: List<
    @Serializable(DurationNanoSerializer::class)
    Duration,
  >? = null,
  @SerialName("metadata")
  val metadata: Map<String, String> = emptyMap(),
  @SerialName("filter_subject")
  val filterSubject: String? = null,
  @SerialName("filter_subjects")
  val filterSubjects: List<String>? = null,
)
