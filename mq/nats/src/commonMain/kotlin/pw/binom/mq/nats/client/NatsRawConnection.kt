@file:Suppress("ktlint:standard:no-wildcard-imports")

package pw.binom.mq.nats.client

import kotlinx.serialization.json.*
// import pw.binom.BINOM_VERSION
import pw.binom.io.*
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.io.socket.InetSocketAddress
import pw.binom.network.SocketClosedException

class NatsRawConnection(
  val channel: AsyncChannel,
//    val onDisconnectWithError: suspend () -> Unit,
//    val onMessage: suspend (Message) -> Unit
) :
  AsyncCloseable {
  private inner class MessageImpl : NatsMessage {
    override var subject: String = ""
    override var sid: String = ""
    override var replyTo: String? = null
    override var headersBody: HeadersBody = HeadersBody.empty
    override var data: ByteArray = ByteArray(0)
    override var headers = NatsHeaders.empty

    override suspend fun ack() {
      TODO("Not yet implemented")
    }
  }

  private val msg = MessageImpl()
  var isConnected = false
  var isConnecting = false
  private var disconnecting = false
  val writer = channel.bufferedAsciiWriter()
  val reader = channel.bufferedAsciiReader()

  data class ConnectInfo(
    val serverId: String,
    val serverName: String,
    val maxPayload: Long,
    val clientId: Int,
    val proto: Int,
    val jetStreamEnabled: Boolean,
    val authRequired: Boolean,
    val clusterAddresses: List<DomainSocketAddress>,
  )

  /**
   * Makes handshake before using nats connection. Returns information about server and other server cluster addresses
   */
  suspend fun prepareConnect(
    clientName: String? = null,
    lang: String = "kotlin",
    echo: Boolean = true,
    tlsRequired: Boolean = false,
    version: String = "0.1.x",
    user: String? = null,
    pass: String? = null,
  ): ConnectInfo {
    check(!isConnecting) { "Connection already in preparing state" }
    check(!isConnected) { "Connection already prepared" }
    isConnecting = true
    try {
      require((user == null && pass == null) || (user != null && pass != null)) { "Invalid User and Password arguments" }

      writer.append("CONNECT {")
        .append("\"verbose\": false, \"tls_required\":").append(tlsRequired)
        .append(", \"lang\":\"").append(lang)
        .append("\",\"version\":\"").append(version).append("\",\"protocol\":1,\"pedantic\":false")
        .append(",\"echo\":").append(echo)

      if (clientName != null) {
        writer.append(",\"name\":\"").append(clientName).append("\"")
      }
      if (user != null && pass != null) {
        writer.append(",\"user\":\"").append(user).append("\"")
          .append(",\"pass\":\"").append(pass).append("\"")
      }

      writer.append("}\r\n")
      writer.flush()
      val connectMsg = reader.readln() ?: throw IOException("Can't connect to Nats")
      val info = parseInfoMsg(connectMsg)
      isConnected = true
      return info
    } finally {
      isConnecting = false
    }
  }

  suspend fun readMessage(): NatsMessage {
    READ_LOOP@ while (true) {
      val msgText = reader.readln() ?: throw SocketClosedException()
      when {
        msgText.startsWith("INFO ") -> parseInfoMsg(msgText)
        msgText == "PING" -> {
          writer.append("PONG\r\n")
          writer.flush()
          continue@READ_LOOP
        }

        msgText.startsWith("MSG ") -> {
          val items = msgText.split(' ')
          val size =
            if (items.size == 5) {
              items[4].toInt()
            } else {
              items[3].toInt()
            }
          msg.subject = items[1]
          msg.sid = items[2]
          msg.replyTo =
            if (items.size == 5) {
              items[3]
            } else {
              null
            }
          val data = ByteArray(size)
          reader.readFully(data)
          reader.readln()
          msg.data = data
          return msg
        }

        else -> throw IOException("Unknown message type. Message: [$msgText]")
      }
    }
  }

  suspend fun subscribe(
    subscribeId: String,
    subject: String,
    group: String?,
  ) {
    check(isConnected && !isConnecting)
    val app = writer
    app.append("SUB ").append(subject)
    if (group != null) {
      app.append(" ").append(group)
    }
    app.append(" ").append(subscribeId).append("\r\n")
    app.flush()
  }

  suspend fun unsubscribe(
    id: String,
    afterMessages: Int? = null,
  ) {
    require(afterMessages == null || afterMessages >= 0)
    writer.append("UNSUB ").append(id)
    if (afterMessages != null) {
      writer.append(" ").append(afterMessages.toString())
    }
    writer.append("\r\n")
    writer.flush()
  }

  suspend fun publish(
    subject: String,
    replyTo: String? = null,
    data: ByteBuffer?,
  ) {
    require(subject.isNotEmpty() && " " !in subject)
    require(replyTo == null || (replyTo.isNotEmpty() && " " !in replyTo))

    writer.append("PUB ").append(subject)
    if (replyTo != null) {
      writer.append(" ").append(replyTo)
    }
    writer.append(" ").append((data?.remaining ?: 0).toString()).append("\r\n")
    writer.flush()
    if (data != null) {
      while (data.remaining > 0) {
        writer.write(data)
      }
    }
    writer.append("\r\n")
    writer.flush()
  }

  suspend fun publish(
    subject: String,
    replyTo: String? = null,
    data: ByteArray?,
  ) {
    val buffer = data?.wrap()
    try {
      publish(
        subject = subject,
        replyTo = replyTo,
        data = buffer,
      )
    } finally {
      buffer?.close()
    }
  }

  override suspend fun asyncClose() {
    disconnecting = true
    isConnected = false
    isConnecting = false
    channel.asyncCloseAnyway()
  }
}

private fun parseInfoMsg(msg: String): NatsRawConnection.ConnectInfo {
  if (!msg.startsWith("INFO ")) {
    throw RuntimeException("Unknown message. Message: [$msg]")
  }
  val json = msg.removePrefix("INFO ")
  return try {
    val data = Json.parseToJsonElement(json)
    NatsRawConnection.ConnectInfo(
      serverId = data.jsonObject["server_id"]?.jsonPrimitive?.content!!,
      serverName = data.jsonObject["server_name"]?.jsonPrimitive?.content!!,
      clientId = data.jsonObject["client_id"]?.jsonPrimitive?.intOrNull!!,
      maxPayload = data.jsonObject["max_payload"]?.jsonPrimitive?.longOrNull!!,
      proto = data.jsonObject["proto"]?.jsonPrimitive?.intOrNull!!,
      jetStreamEnabled = data.jsonObject["jetstream"]?.jsonPrimitive?.booleanOrNull ?: false,
      authRequired = data.jsonObject["auth_required"]?.jsonPrimitive?.booleanOrNull ?: false,
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
