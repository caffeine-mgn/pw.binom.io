package pw.binom.mq.nats.client

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer
import pw.binom.mq.Headers
import pw.binom.mq.Message
import pw.binom.mq.nats.client.dto.*
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.time.Duration

class JetStreamImpl(val reader: NatsReader) {
  companion object;

  suspend fun publish(
    subject: String,
    headers: HeadersBody,
    data: ByteArray,
  ) {
    reader.connection.publish(
      subject = subject,
      headers = headers,
      data = data,
    )
  }

  suspend fun publish(
    subject: String,
    headers: HeadersBody,
    body: ByteBuffer,
  ) {
    reader.connection.publish(
      subject = subject,
      headers = headers,
      data = body,
    )
  }

  suspend fun createConsumer(
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
      reader.sendAndReceive(
        subject = s,
        data = Json.encodeToString(ConsumerCreateRequestDto.serializer(), req).encodeToByteArray(),
      )
    return JetStreamApiJsonUtils.decode(ConsumerCreateResponseDto.serializer(), msg.data)
  }

  suspend fun create(config: StreamConfig): StreamCreateResponseDto {
    val txt = JetStreamApiJsonUtils.encode(StreamConfig.serializer(), config)
    val msg =
      reader.sendAndReceive(subject = "\$JS.API.STREAM.CREATE.${config.name}", data = txt)
    return JetStreamApiJsonUtils.decode(
      serializer = StreamCreateResponseDto.serializer(),
      data = msg.data,
    )
  }

  suspend fun updateStream(config: StreamConfig): StreamUpdateResponseDto {
    val txt = JetStreamApiJsonUtils.encode(StreamConfig.serializer(), config)
    val msg =
      reader.sendAndReceive(subject = "\$JS.API.STREAM.UPDATE.${config.name}", data = txt)
    return JetStreamApiJsonUtils.decode(
      serializer = StreamUpdateResponseDto.serializer(),
      data = msg.data,
    )
  }

  suspend fun deleteStream(name: String) {
    val msg = reader.sendAndReceive(subject = "\$JS.API.STREAM.DELETE.$name", data = null as ByteArray?)
    JetStreamApiJsonUtils.checkError(msg.data) { throw RuntimeException("Can't delete stream: $it") }
  }

  suspend fun deleteMessage(
    streamName: String,
    seq: Long,
    erase: Boolean = true,
  ) {
    val payload =
      JetStreamApiJsonUtils.encode(
        MessageDeleteRequestDto.serializer(),
        MessageDeleteRequestDto(
          sequence = seq,
          noErase = !erase,
        ),
      )
    val msg = reader.sendAndReceive(subject = "\$JS.API.STREAM.MSG.DELETE.$streamName", data = payload)
    JetStreamApiJsonUtils.checkError(msg.data) { throw RuntimeException("Can't delete message from stream $streamName: $it") }
  }

  suspend fun deleteConsumer(
    streamName: String,
    consumerName: String,
  ) {
    val msg =
      reader.sendAndReceive(subject = "\$JS.API.CONSUMER.DELETE.$streamName.$consumerName", data = null as ByteArray?)
    JetStreamApiJsonUtils.checkError(msg.data) { throw RuntimeException("Can't delete consumer $consumerName on stream $streamName: $it") }
  }

  suspend fun getMessage(
    streamName: String,
    config: MessageGetRequestDto,
  ): MessageInfoDto {
    val streamInfo =
      getStreamInfo(
        name = streamName,
      )
    if (streamInfo.config?.allowDirect == true) {
      val resp =
        if (config.isLastBySubject) {
          reader.sendAndReceive(
            subject = "\$JS.API.DIRECT.GET.$streamName.${config.lastBySubject}",
            data = null as ByteArray?,
          )
        } else {
          reader.sendAndReceive(
            subject = "\$JS.API.DIRECT.GET.$streamName",
            data = JetStreamApiJsonUtils.encode(MessageGetRequestDto.serializer(), config),
          )
        }
      // TODO добавить проверку
//      if (resp.isStatusMessage()) {
//        throw new JetStreamApiException(Error.convert(resp.getStatus()));
//      }
      return MessageInfoDto.create(msg = resp, direct = true, streamName = streamName) {
        throw RuntimeException("Can't get message: $it")
      }
    } else {
      val msg =
        reader.sendAndReceive(
          subject = "\$JS.API.STREAM.MSG.GET.$streamName",
          data = JetStreamApiJsonUtils.encode(MessageGetRequestDto.serializer(), config),
        )

      return MessageInfoDto.create(msg, direct = false, streamName = streamName) {
        throw RuntimeException("Can't get message: $it")
      }
    }
  }

  suspend fun purgeStream(
    streamName: String,
    config: PurgeOptionsDto? = null,
  ) {
    val msg =
      reader.sendAndReceive(
        subject = "\$JS.API.STREAM.PURGE.$streamName",
        data = config?.let { JetStreamApiJsonUtils.encode(PurgeOptionsDto.serializer(), it) },
      )
    JetStreamApiJsonUtils.checkError(msg.data) { throw RuntimeException("Can't purge stream: $it") }
  }

  suspend fun getStreamInfoAll(
    name: String,
    deletedDetails: Boolean = false,
    subjectsFilter: String? = null,
  ): StreamInfoResponseDto {
    var offset = 0
    var total = 0
    var limit = 0
    var streamInfo: StreamInfoResponseDto? = null
    val ss = HashMap<String, Long>()
    do {
      val e =
        getStreamInfo(
          name = name,
          offset = offset + limit,
          deletedDetails = deletedDetails,
          subjectsFilter = subjectsFilter,
        )
      if (streamInfo == null) {
        streamInfo = e
      }
      val ee = e.state?.subjects ?: break
      ee.forEach {
        ss[it.key] = it.value
      }
      limit = e.limit ?: 0
      total = e.total ?: 0
      offset = e.offset ?: 0
    } while (offset < total)
    val resultStreamInfo = streamInfo ?: TODO()
    return resultStreamInfo.copy(
      state = resultStreamInfo.state?.copy(subjects = ss),
      offset = total,
      limit = 0,
    )
  }

  suspend fun getStreamInfo(
    name: String,
    offset: Int = 0,
    deletedDetails: Boolean = false,
    subjectsFilter: String? = null,
  ): StreamInfoResponseDto {
    @Serializable
    data class Cursor(
      val offset: Int,
      val subjects_filter: String?,
      val deleted_details: Boolean? = null,
    )

    val opt =
      JetStreamApiJsonUtils.encode(
        Cursor.serializer(),
        Cursor(
          offset = offset,
          subjects_filter = subjectsFilter,
          deleted_details = deletedDetails,
        ),
      )
    val msg =
      reader.sendAndReceive(subject = "\$JS.API.STREAM.INFO.$name", data = opt)
    return JetStreamApiJsonUtils.decode(
      serializer = StreamInfoResponseDto.serializer(),
      data = msg.data,
    )
  }

  suspend fun getConsumerInfo(
    streamName: String,
    consumerName: String,
  ): ConsumerInfoResponseDto {
    val msg =
      reader.sendAndReceive(
        subject = "\$JS.API.CONSUMER.INFO.$streamName.$consumerName",
        data = null as ByteArray?,
      )
    return JetStreamApiJsonUtils.decode(
      ConsumerInfoResponseDto.serializer(),
      data = msg.data,
    )
  }

  private inner class MessageWithAck(val msg: NatsMessage) : Message {
    override val headers: Headers
      get() = msg.headers
    override val topic: String
      get() = msg.topic
    override val body: ByteArray
      get() = msg.body

    override suspend fun ack() {
      val replyTo = msg.replyTo
      if (replyTo != null) {
        sendAck(subject = replyTo)
      }
    }

    override fun toString(): String = msg.toString()
  }

  suspend fun receiveMessage(
    streamName: String,
    consumerName: String,
    config: PullRequestOptionsDto,
    incomeListener: suspend (Message) -> Unit,
    withAckSupport: Boolean = true,
  ): AsyncCloseable {
    val into = Random.nextUuid().toString()
    val newConfig = if (config.batch <= 0) config.copy(batch = 100) else config
    var remaining = newConfig.batch

    suspend fun pullNext() {
      remaining = newConfig.batch
      pullMessages(
        streamName = streamName,
        consumerName = consumerName,
        into = into,
        config = newConfig,
      )
    }

    val listener: (suspend (NatsMessage) -> Unit) =
      if (withAckSupport) {
        { msg ->
          incomeListener(MessageWithAck(msg))
        }
      } else {
        incomeListener
      }
    val readNext = AtomicBoolean(true)
    var subscribeClosable: AsyncCloseable? = null
    subscribeClosable =
      this.reader.subscribe(
        subject = into,
      ) { msg ->
        if (readNext.getValue()) {
          listener(msg)
          remaining--
          if (remaining == 0) {
            pullNext()
          }
        } else {
          subscribeClosable!!.asyncCloseAnyway()
        }
      }
    pullNext()
    return AsyncCloseable {
      if (readNext.compareAndSet(true, false)) {
        if (remaining == 0) {
          subscribeClosable.asyncCloseAnyway()
        }
      }
    }
  }

  suspend fun pullMessages(
    streamName: String,
    consumerName: String,
    into: String,
    config: PullRequestOptionsDto,
  ) {
    reader.connection.publish(
      subject = "\$JS.API.CONSUMER.MSG.NEXT.$streamName.$consumerName",
      replyTo = into,
      data = JetStreamApiJsonUtils.encode(PullRequestOptionsDto.serializer(), config),
    )
  }

  suspend fun sendAck(
    subject: String,
    delay: Duration = Duration.ZERO,
  ) {
    val type = AckType.AckAck
    val bytes = type.bytes
    val text = type.text
    reader.connection.publish(
      subject = subject,
      data =
        if (delay.isNegative() || delay == Duration.ZERO) {
          bytes
        } else {
          "$text {\"delay\": ${delay.inWholeNanoseconds}}".encodeToByteArray()
        },
    )
  }
}
