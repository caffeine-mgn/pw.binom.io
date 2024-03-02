@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.mq.nats.client

import kotlinx.serialization.json.*
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.*
import pw.binom.io.socket.NetworkAddress
import pw.binom.mq.nats.client.dto.ConnectRequestDto
import pw.binom.network.SocketClosedException

class InternalNatsConnection private constructor(
  override val config: ConnectInfo,
  private val writer: AsyncBufferedAsciiWriter,
  private val reader: AsyncBufferedAsciiInputReader,
  private val channel: AsyncChannel,
  private val headerEnabled: Boolean,
) : NatsConnection {
  companion object {
    private fun parseInfoMsg(msg: String): ConnectInfo {
      if (!msg.startsWith("INFO ")) {
        throw RuntimeException("Unknown message. Message: [$msg]")
      }
      val json = msg.removePrefix("INFO ")
      return try {
        val data = Json.parseToJsonElement(json)
        ConnectInfo(
          serverId = data.jsonObject["server_id"]?.jsonPrimitive?.content!!,
          serverName = data.jsonObject["server_name"]?.jsonPrimitive?.content!!,
          clientId = data.jsonObject["client_id"]?.jsonPrimitive?.intOrNull!!,
          maxPayload = data.jsonObject["max_payload"]?.jsonPrimitive?.longOrNull!!,
          proto = data.jsonObject["proto"]?.jsonPrimitive?.intOrNull!!,
          jetStreamEnabled = data.jsonObject["jetstream"]?.jsonPrimitive?.booleanOrNull ?: false,
          authRequired = data.jsonObject["auth_required"]?.jsonPrimitive?.booleanOrNull ?: false,
          headersSupported = data.jsonObject["headers"]?.jsonPrimitive?.booleanOrNull ?: false,
          clusterAddresses =
            data.jsonObject["connect_urls"]?.jsonArray?.map {
              val items = it.jsonPrimitive.content.split(':')

              NetworkAddress.create(
                host = items[0],
                port = items[1].toInt(),
              )
            } ?: emptyList(),
        )
      } catch (e: Throwable) {
        throw RuntimeException("Can't parse info message. Message: [$json]", e)
      }
    }

    suspend fun connect(
      channel: AsyncChannel,
      clientName: String? = null,
      lang: String = "kotlin",
      echo: Boolean = true,
      tlsRequired: Boolean = false,
      version: String = "0.1.x",
      headers: Boolean = true,
      auth: Auth? = null,
      readBufferSize: Int = DEFAULT_BUFFER_SIZE,
      writeBufferSize: Int = DEFAULT_BUFFER_SIZE,
    ): InternalNatsConnection {
      val writer = channel.bufferedAsciiWriter(bufferSize = writeBufferSize, closeParent = false)
      val reader = channel.bufferedAsciiReader(bufferSize = readBufferSize, closeParent = false)
      val connectRequest =
        ConnectRequestDto(
          lang = lang,
          pedantic = false,
          tlsRequired = tlsRequired,
          version = version,
          verbose = false,
          headers = headers,
          name = clientName,
          user = auth?.user,
          pass = auth?.password,
          echo = echo,
        )
      try {
        writer.append("CONNECT ")
          .append(Json.encodeToString(ConnectRequestDto.serializer(), connectRequest))
          .append("\r\n")

        writer.flush()
        val connectMsg = reader.readln() ?: throw IOException("Can't connect to Nats")
        val info = parseInfoMsg(connectMsg)
        return InternalNatsConnection(
          config = info,
          writer = writer,
          reader = reader,
          channel = channel,
          headerEnabled = headers,
        )
      } catch (e: Throwable) {
        writer.asyncCloseAnyway()
        reader.asyncCloseAnyway()
        throw e
      }
    }
  }

  private class MessageImpl : NatsMessage {
    override var subject: String = ""
    override var sid: String = ""
    override var replyTo: String? = null
    override var headersBody: HeadersBody = HeadersBody.empty
    override var data: ByteArray = ByteArray(0)
    override var headers = NatsHeaders.empty

    override suspend fun ack() {
    }

    override fun toString() =
      "Message(subject='$subject', sid='$sid', replyTo=$replyTo, headers=$headersBody, data=${data.contentToString()})"
  }

  private val msg = MessageImpl()

  private suspend fun parseMsg(msgText: String): MessageImpl {
    val items = msgText.split(' ', limit = 5)
    var cursor = 1
    msg.subject = items[cursor++]
    msg.sid = items[cursor++]
    msg.replyTo =
      if (items.size == 5) {
        items[cursor++]
      } else {
        null
      }
    val size = items[cursor++].toInt()
    val data = ByteArray(size)
    reader.readFully(data)
    reader.skip(2)
    msg.headersBody = HeadersBody.empty
    msg.headers = msg.headersBody.parse()
    msg.data = data
    return msg
  }

  private suspend fun parseHMsg(msgText: String): MessageImpl {
    val items = msgText.split(' ', limit = 6)
    var cursor = 1
    msg.subject = items[cursor++]
    msg.sid = items[cursor++]
    msg.replyTo =
      if (items.size == 6) {
        items[cursor++]
      } else {
        null
      }
    val headerSize = items[cursor++].toInt()
    val bodySize = items[cursor++].toInt() - headerSize

    val header = ByteArray(headerSize - 2)
    reader.readFully(header)
    reader.skip(2)
    val body = ByteArray(bodySize)
    reader.readFully(body)
    msg.headersBody = HeadersBody(header)
    msg.headers = msg.headersBody.parse()
    msg.data = body
    reader.skip(2)
    return msg
  }

  override suspend fun readMessage(): NatsMessage {
    READ_LOOP@ while (true) {
      val msgText = reader.readln() ?: throw SocketClosedException()
      when {
        msgText.startsWith("INFO ") -> parseInfoMsg(msgText)
        msgText == "PING" -> {
          writer.append("PONG\r\n")
          writer.flush()
          continue@READ_LOOP
        }

        msgText.startsWith("MSG ") -> return parseMsg(msgText)
        msgText.startsWith("HMSG ") -> return parseHMsg(msgText)

        else -> throw IOException("Unknown message type. Message: [$msgText]")
      }
    }
  }

  private suspend inline fun internalPublish(
    subject: String,
    replyTo: String?,
    headers: HeadersBody,
    dataSize: Int,
    data: (AsyncBufferedAsciiWriter) -> Unit,
  ) {
    require(subject.isNotEmpty() && " " !in subject)
    require(replyTo == null || (replyTo.isNotEmpty() && " " !in replyTo))

    if (headers.isEmpty) {
      writer.append("PUB ")
        .append(subject)
      if (replyTo != null) {
        writer.append(" ").append(replyTo)
      }
      writer.append(" ").append(dataSize.toString()).append("\r\n")
      data(writer)
      writer.append("\r\n")
      writer.flush()
    } else {
      check(config.headersSupported && headerEnabled) { "Headers not supported" }
      writer.append("HPUB ")
        .append(subject)
      if (replyTo != null) {
        writer.append(" ").append(replyTo)
      }
      val headersSize = headers.bytes.size + 2
      val totalSize = dataSize + headersSize
      writer.append(" ")
        .append(headersSize)
        .append(" ").append(totalSize).append("\r\n")
      writer.write(headers.bytes)
      writer.append("\r\n")
      data(writer)
      writer.append("\r\n")
      writer.flush()
      return
    }
  }

  override suspend fun publish(
    subject: String,
    replyTo: String?,
    headers: HeadersBody,
    data: ByteBuffer?,
  ) {
    internalPublish(
      subject = subject,
      replyTo = replyTo,
      headers = headers,
      dataSize = data?.remaining ?: 0,
    ) {
      data?.let { bytes -> it.write(bytes) }
    }
  }

  override suspend fun subscribe(
    subject: String,
    group: String?,
    subscribeId: String,
  ) {
    writer.append("SUB ").append(subject)
    if (group != null) {
      writer.append(" ").append(group)
    }
    writer.append(" ").append(subscribeId).append("\r\n")
    writer.flush()
  }

  override suspend fun unsubscribe(
    id: String,
    afterMessages: Int,
  ) {
    writer.append("UNSUB ").append(id)
    if (afterMessages > 0) {
      writer.append(" ").append(afterMessages.toString())
    }
    writer.append("\r\n")
    writer.flush()
  }

  override suspend fun publish(
    subject: String,
    replyTo: String?,
    headers: HeadersBody,
    data: ByteArray?,
  ) {
    internalPublish(
      subject = subject,
      replyTo = replyTo,
      headers = headers,
      dataSize = data?.size ?: 0,
    ) {
      data?.let { bytes -> it.write(bytes) }
    }
  }

  override suspend fun asyncClose() {
    channel.asyncClose()
  }
}
