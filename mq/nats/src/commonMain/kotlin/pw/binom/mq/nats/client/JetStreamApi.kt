package pw.binom.mq.nats.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pw.binom.mq.nats.client.dto.ConsumerCreateRequestDto
import pw.binom.mq.nats.client.dto.ConsumerCreateResponseDto
import pw.binom.mq.nats.client.dto.ConsumerInfoResponseDto
import pw.binom.mq.nats.client.dto.PullRequestOptionsDto
import pw.binom.mq.nats.client.dto.PurgeOptionsDto
import pw.binom.mq.nats.client.dto.StreamConfig
import pw.binom.mq.nats.client.dto.StreamCreateResponseDto
import pw.binom.mq.nats.client.dto.StreamInfoResponseDto
import kotlin.time.Duration

internal object JetStreamApi {
  suspend fun getConsumerInfo(
    streamName: String,
    consumerName: String,
    client: NatsConnection,
  ): ConsumerInfoResponseDto {
    val msg =
      client.sendAndReceive(
        subject = "\$JS.API.CONSUMER.INFO.$streamName.$consumerName",
        data = null as ByteArray?,
      )
    return JetStreamApiJsonUtils.decode(
      ConsumerInfoResponseDto.serializer(),
      data = msg.data,
      checkError = false,
    )
  }

  suspend fun pullMessages(
    streamName: String,
    consumerName: String,
    into: String,
    config: PullRequestOptionsDto,
    client: NatsConnection,
  ) {
    client.send(
      subject = "\$JS.API.CONSUMER.MSG.NEXT.$streamName.$consumerName",
      replyTo = into,
      data = JetStreamApiJsonUtils.encode(PullRequestOptionsDto.serializer(), config),
    )
  }

  suspend fun sendAck(
    subject: String,
    delay: Duration = Duration.ZERO,
    client: NatsConnection,
  ) {
    val type = AckType.AckAck
    val bytes = type.bytes
    val text = type.text
    client.send(
      subject = subject,
      data =
        if (delay.isNegative() || delay == Duration.ZERO) {
          bytes
        } else {
          "$text {\"delay\": ${delay.inWholeNanoseconds}}".encodeToByteArray()
        },
    )
  }

  suspend fun getStreamInfo(
    name: String,
    offset: Int = 0,
    deletedDetails: Boolean = false,
    subjectsFilter: String? = null,
    client: NatsConnection,
  ): StreamInfoResponseDto {
    @Serializable
    data class Cursor(
      val offset: Int,
      @SerialName("subjects_filter")
      val subjectsFilter: String?,
      @SerialName("deleted_details")
      val deletedDetails: Boolean? = null,
    )

    val opt =
      JetStreamApiJsonUtils.encode(
        Cursor.serializer(),
        Cursor(
          offset = offset,
          subjectsFilter = subjectsFilter,
          deletedDetails = deletedDetails,
        ),
      )
    val msg =
      client.sendAndReceive(subject = "\$JS.API.STREAM.INFO.$name", data = opt)
    return JetStreamApiJsonUtils.decode(
      serializer = StreamInfoResponseDto.serializer(),
      data = msg.data,
      checkError = false,
    )
  }

  suspend fun getLastMessage(
    stream: String,
    subject: String,
    client: NatsConnection,
  ) = client.sendAndReceive(
    subject = "\$JS.API.DIRECT.GET.$stream.$subject",
    data = null as ByteArray?
  )

  suspend fun purgeStream(
    streamName: String,
    config: PurgeOptionsDto? = null,
    client: NatsConnection,
  ) {
    val msg =
      client.sendAndReceive(
        subject = "\$JS.API.STREAM.PURGE.$streamName",
        data = config?.let { JetStreamApiJsonUtils.encode(PurgeOptionsDto.serializer(), it) },
      )
    JetStreamApiJsonUtils.checkError(msg.data) { throw RuntimeException("Can't purge stream: $it") }
  }

  suspend fun deleteConsumer(
    streamName: String,
    consumerName: String,
    client: NatsConnection,
  ) {
    val msg =
      client.sendAndReceive(subject = "\$JS.API.CONSUMER.DELETE.$streamName.$consumerName", data = null as ByteArray?)
    JetStreamApiJsonUtils.checkError(msg.data) { throw RuntimeException("Can't delete consumer $consumerName on stream $streamName: $it") }
  }


  suspend fun createStream(config: StreamConfig,client: NatsConnection): StreamCreateResponseDto {
    val txt = JetStreamApiJsonUtils.encode(StreamConfig.serializer(), config)
    val msg =
      client.sendAndReceive(subject = "\$JS.API.STREAM.CREATE.${config.name}", data = txt)
    println("--->$msg")
    return JetStreamApiJsonUtils.decode(
      serializer = StreamCreateResponseDto.serializer(),
      data = msg.data,
    )
  }

  suspend fun createConsumer(
    client: NatsConnection,
    streamName: String,
    config: ConsumerConfiguration,
  ): ConsumerCreateResponseDto {
    val s =
      if (config.durableName == null) {
        "\$JS.API.CONSUMER.CREATE.$streamName"
      } else {
        "\$JS.API.CONSUMER.DURABLE.CREATE.$streamName.${config.durableName}"
      }
    val req =
      ConsumerCreateRequestDto(
        streamName = streamName,
        config = config,
      )

    val msg =
      client.sendAndReceive(
        subject = s,
        data = Json.encodeToString(ConsumerCreateRequestDto.serializer(), req).encodeToByteArray(),
      )
    return JetStreamApiJsonUtils.decode(ConsumerCreateResponseDto.serializer(), msg.data)
  }
}
