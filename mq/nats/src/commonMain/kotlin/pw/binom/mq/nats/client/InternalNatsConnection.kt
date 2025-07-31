@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.mq.nats.client

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.coroutines.SimpleAsyncLock
import pw.binom.io.*
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.mq.nats.client.dto.ConnectRequestDto
import pw.binom.mq.nats.parseHeaders
import pw.binom.network.SocketClosedException

class InternalNatsConnection private constructor(
  override val config: ConnectInfo,
  private val channel: AsyncChannelPair<AsyncBufferedAsciiInputReader, AsyncBufferedOutput>,
//  private val writer: AsyncBufferedAsciiWriter,
//  private val reader: AsyncBufferedAsciiInputReader,
//  private val channel: AsyncChannel,
  private val headerEnabled: Boolean,
  private val onDisconnect: (() -> Unit)? = null,
) : NatsProtoConnection {
  companion object {
    private val CLRF = "\r\n".encodeToByteArray()
    private val SPACE=" ".encodeToByteArray()
    private val CMD_PUB="PUB ".encodeToByteArray()
    private val CMD_HPUB="HPUB ".encodeToByteArray()
    private val CMD_SUB="SUB ".encodeToByteArray()
    private val CMD_UNSUB="UNSUB ".encodeToByteArray()
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
              DomainSocketAddress(
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
      channel: AsyncChannelPair<AsyncInput, AsyncOutput>,
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
      val bufferedChannel=AsyncChannelPair.create(
        input = channel.input.bufferedAsciiReader(bufferSize = readBufferSize),
        output = channel.output.bufferedOutput(bufferSize = writeBufferSize),
      )
//      val bufferedChannel = channel.buffered(
//        readBufferSize = readBufferSize,
//        writeBufferSize = writeBufferSize,
//      )
//      val writer = channel.bufferedAsciiWriter(bufferSize = writeBufferSize, closeParent = false)
//      val reader = channel.bufferedAsciiReader(bufferSize = readBufferSize, closeParent = false)
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
        bufferedChannel.output.write("CONNECT ".encodeToByteArray())
        bufferedChannel.output.write(Json.encodeToString(ConnectRequestDto.serializer(), connectRequest).encodeToByteArray())
        bufferedChannel.output.write(CLRF)

        bufferedChannel.output.flush()
        val connectMsg = bufferedChannel.input.readln() ?: throw IOException("Can't connect to Nats")
        val info = parseInfoMsg(connectMsg)
        return InternalNatsConnection(
          config = info,
          channel = bufferedChannel,
//          writer = writer,
//          reader = reader,
//          channel = channel,
          headerEnabled = headers,
        )
      } catch (e: Throwable) {
        bufferedChannel.asyncCloseAnyway()
        throw e
      }
    }

    /**
     * \r
     */
    private const val CR: Byte = 0x0d

    /**
     * \n
     */
    private const val LF: Byte = 0x0a
    private val LINE_END = byteArrayOf(0x0d, 0x0a)
  }

  private val reader = channel.input
  private val writer = channel.output

  private class MessageImpl : NatsMessage {
    override var subject: String = ""
    override var subscribeId: String = ""
    override var replyTo: String? = null
    override var data: ByteArray = ByteArray(0)
    override var headers = NatsHeaders.empty

    override suspend fun ack() {
    }

    override fun toString() =
      "Message(subject='$subject', sid='$subscribeId', replyTo=$replyTo, headers=$headers, data=${data.contentToString()})"
  }

  private class NatsMessageImpl2(
    override val subject: String,
    override val subscribeId: String,
    override val replyTo: String?,
    override val data: ByteArray,
    override val headers: NatsHeaders,
  ) : NatsMessage {
    override suspend fun ack() {
    }
  }

  private val msg = MessageImpl()
  private val writeLock = SimpleAsyncLock()

  private suspend fun parseMsg(msgText: String): NatsMessageImpl2 {
    val items = msgText.split(' ', limit = 5)
    var cursor = 1
    val subject = items[cursor++]
    val sid = items[cursor++]
    val replyTo =
      if (items.size == 5) {
        items[cursor++]
      } else {
        null
      }
    val size = items[cursor++].toInt()
    val data = ByteArray(size)
    reader.readFully(data)
    reader.skip(2)
    val headersBody = HeadersBody.empty
    return NatsMessageImpl2(
      subject = subject,
      subscribeId = sid,
      replyTo = replyTo,
      data = data,
      headers = NatsHeaders.empty,
    )
  }

  private suspend fun parseHMsg(msgText: String): NatsMessageImpl2 {
    val items = msgText.split(' ', limit = 6)
    var cursor = 1
    val subject = items[cursor++]
    val sid = items[cursor++]
    val replyTo =
      if (items.size == 6) {
        items[cursor++]
      } else {
        null
      }
    val headerSize = items[cursor++].toInt()
    val bodySize = items[cursor++].toInt() - headerSize

    val header = ByteArray(headerSize - 2)
    reader.readFully(header)
    val headers = parseHeaders(header)
    reader.skip(2)
    val body = if (bodySize > 0) {
      val b = ByteArray(bodySize)
      reader.readFully(b)
      b
    } else {
      byteArrayOf()
    }


    val data = body
    reader.skip(2)

    return NatsMessageImpl2(
      subject = subject,
      subscribeId = sid,
      replyTo = replyTo,
      data = data,
      headers = headers,
    )
  }

  override suspend fun readMessage(): NatsMessage {
    try {
      READ_LOOP@ while (true) {
        val msgText = reader.readln() ?: throw SocketClosedException()
        when {
          msgText.startsWith("INFO ") -> parseInfoMsg(msgText)
          msgText == "PING" -> {
            writeLock.synchronize {
              writer.write("PONG".encodeToByteArray())
              writer.write(CLRF)
              writer.flush()
            }
            continue@READ_LOOP
          }

          msgText.startsWith("MSG ") -> return parseMsg(msgText)
          msgText.startsWith("HMSG ") -> return parseHMsg(msgText)
          msgText.startsWith("-ERR '") -> {
            val error = msgText.removePrefix("-ERR '").removeSuffix("'")
            println("ERROR: $error")
            continue@READ_LOOP
          }

          else -> throw IOException("Unknown message type. Message: [$msgText]")
        }
      }
    } catch (e: StreamClosedException) {
      onDisconnect()
      throw e
    }
  }

  private suspend inline fun internalPublish(
    subject: String,
    replyTo: String?,
    headers: HeadersBody,
    dataSize: Int,
    data: (AsyncOutput) -> Unit,
  ) {
    require(subject.isNotEmpty() && " " !in subject)
    require(replyTo == null || (replyTo.isNotEmpty() && " " !in replyTo))
    check(headers.isEmpty || (config.headersSupported && headerEnabled)) { "Headers not supported" }
    try {
      writeLock.lock()
      if (headers.isEmpty) {
        writer.write(CMD_PUB)
      } else {
        writer.write(CMD_HPUB)
      }
      writer.write(subject.encodeToByteArray())
      if (replyTo != null) {
        writer.write(SPACE)
        writer.write(replyTo.toString().encodeToByteArray())
      }
      if (headers.isEmpty) {
        writer.write(SPACE)
        writer.write(dataSize.toString().encodeToByteArray())
      } else {
        val headersSize = headers.bytes.size + 2
        val totalSize = dataSize + headersSize
        writer.write(SPACE)
        writer.write(headersSize.toString().encodeToByteArray())
        writer.write(SPACE)
        writer.write(totalSize.toString().encodeToByteArray())
        writer.write(CLRF)
        writer.write(headers.bytes)
      }
      writer.write(CLRF)
      writer.flush()
      data(writer)
      writer.write(CLRF)
      writer.flush()
    } catch (e: StreamClosedException) {
      onDisconnect()
    } finally {
      writeLock.unlock()
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
      data?.let { bytes -> it.writeFully(bytes) }
    }
  }

  override suspend fun subscribe(
    subject: String,
    group: String?,
    subscribeId: String,
  ) {
    writeLock.synchronize {
      writer.write(CMD_SUB)
      writer.write(subject.encodeToByteArray())
      if (group != null) {
        writer.write(SPACE)
        writer.write(group.encodeToByteArray())
      }
      writer.write(SPACE)
      writer.write(subscribeId.encodeToByteArray())
      writer.write(CLRF)
      writer.flush()
    }
  }

  override suspend fun unsubscribe(
    subscribeId: String,
    afterMessages: Int,
  ) {
    writeLock.synchronize {
      writer.write(CMD_UNSUB)
      writer.write(subscribeId.encodeToByteArray())
      if (afterMessages > 0) {
        writer.write(SPACE)
        writer.write(afterMessages.toString().encodeToByteArray())
      }
      writer.write(CLRF)
      writer.flush()
    }
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
      data?.let { bytes ->
        it.writeFully(bytes)
      }
    }
  }

  private fun onDisconnect() {
    onDisconnect?.invoke()
  }

  override suspend fun asyncClose() {
    channel.asyncClose()
  }
}
