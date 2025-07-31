package pw.binom.mq.nats.client

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer
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
    if (streamInfo.error?.code == ErrorDto.NOT_FOUND) {
      throw RuntimeException("Stream $streamName not found")
    }
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
      checkError = false,
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
      checkError = false,
    )
  }

  private inner class MessageWithAck(val msg: NatsMessage) : NatsMessage by msg {

    override suspend fun ack() {
      val replyTo = msg.replyTo
      if (replyTo != null) {
        sendAck(subject = replyTo)
      }
    }
  }

  suspend fun pullNext(
    streamName: String,
    consumerName: String,
    config: PullRequestOptionsDto,
    withAckSupport: Boolean = true,
  ): Flow<NatsMessage> {
    val newConfig = config.minBatch(1)
    val into = Random.nextUuid().toString()
    var remaining = newConfig.batch
    val channel = Channel<NatsMessage>(capacity = config.batch)
    var listener: AsyncCloseable? = null
    listener = reader.subscribe(
      subject = into,
    ) { msg ->
      if (msg.headers.code == NatsHeaders.CODE_CONSUMER_HEARTBEAT) {
        return@subscribe
      }
      if (msg.headers.code == NatsHeaders.CODE_CONSUME_TIMEOUT || msg.headers.code == NatsHeaders.CODE_CONSUMER_DELETED) {
        channel.close()
        listener?.asyncCloseAnyway()
        return@subscribe
      }
      channel.send(msg)
      remaining--
      if (remaining <= 0) {
        channel.close()
        listener?.asyncCloseAnyway()
      }
    }
    pullMessages(
      streamName = streamName,
      consumerName = consumerName,
      into = into,
      config = newConfig,
    )
    val flow = channel.receiveAsFlow()
    return if (withAckSupport) {
      flow.map { MessageWithAck(it) }
    } else {
      flow
    }
  }

  suspend fun receiveMessage(
    streamName: String,
    consumerName: String,
    config: PullRequestOptionsDto,
    incomeListener: suspend (NatsMessage) -> Unit,
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

  suspend fun getLastMessage(
    stream: String,
    subject: String,
  ) = reader.sendAndReceive(
    subject = "\$JS.API.DIRECT.GET.%s.%s.$stream.$subject",
    data = null as ByteArray?
  )

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
