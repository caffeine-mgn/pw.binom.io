package pw.binom.mq.nats.client

import kotlinx.serialization.json.*
import pw.binom.BINOM_VERSION
import pw.binom.ByteBuffer
import pw.binom.UUID
import pw.binom.io.*
import pw.binom.network.NetworkAddress
import pw.binom.network.SocketClosedException
import pw.binom.wrap

class NatsRawConnection(
    val channel: AsyncChannel,
//    val onDisconnectWithError: suspend () -> Unit,
//    val onMessage: suspend (Message) -> Unit
) :
    AsyncCloseable {

    private val zeroBuff = ByteBuffer.alloc(0)

    private inner class MessageImpl : NatsMessage {
        override val connection: NatsRawConnection
            get() = this@NatsRawConnection
        override var subject: String = ""
        override var sid: UUID = UUID.random()
        override var replyTo: String? = null
        override var data: ByteArray = ByteArray(0)
    }

    private val msg = MessageImpl()
    var isConnected = false
    var isConnecting = false
    private var disconnecting = false
    private val writer = channel.bufferedAsciiWriter()
    private val reader = channel.bufferedAsciiReader()

    data class ConnectInfo(
        val serverId: String,
        val serverName: String,
        val maxPayload: Long,
        val clientId: Int,
        val proto: Int,
        val clusterAddresses: List<NetworkAddress>
    )

    /**
     * Makes handshake before using nats connection. Retuns information about server and other server cluster addresses
     */
    suspend fun prepareConnect(
        clientName: String? = null,
        lang: String = "kotlin",
        echo: Boolean = true,
        tlsRequired: Boolean = false,
        version: String = BINOM_VERSION,
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
            println("Nats Info: $connectMsg")
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
                    val size = if (items.size == 5) {
                        items[4].toInt()
                    } else {
                        items[3].toInt()
                    }
                    msg.subject = items[1]
                    msg.sid = UUID.fromString(items[2])
                    msg.replyTo = if (items.size == 5) {
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
                else -> throw IOException("Unknown message type. Message: [$msg]")
            }
        }
    }

//    fun processing() {
//        check(isConnected && !isConnecting)
//        async2 {
//            try {
//                while (true) {
//                    processMessage()
//                }
//            } catch (e: Throwable) {
//                if (!disconnecting) {
//                    onDisconnectWithError()
//                }
//            }
//        }
//    }

    suspend fun subscribe(subscribeId: UUID, subject: String, group: String?) {
        check(isConnected && !isConnecting)
        val app = writer
        app.append("SUB ").append(subject)
        if (group != null) {
            app.append(" ").append(group)
        }
        app.append(" ").append(subscribeId.toString()).append("\r\n")
        app.flush()
    }

    suspend fun unsubscribe(id: UUID, afterMessages: Int? = null) {
        require(afterMessages == null || afterMessages >= 0)
        writer.append("UNSUB ").append(id.toString())
        if (afterMessages != null) {
            writer.append(" ").append(afterMessages.toString())
        }
        writer.append("\r\n")
        writer.flush()
    }

    suspend fun publish(subject: String, replyTo: String? = null, data: ByteBuffer?) {
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

    suspend fun publish(subject: String, replyTo: String? = null, data: ByteArray?) {
        val wrapedData = data?.let { ByteBuffer.wrap(it) }
        try {
            publish(
                subject = subject,
                replyTo = replyTo,
                data = wrapedData
            )
        } finally {
            wrapedData?.close()
        }
    }

    override suspend fun asyncClose() {
        zeroBuff.close()
        disconnecting = true
        isConnected = false
        isConnecting = false
        runCatching { channel.asyncClose() }
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
            clusterAddresses = data.jsonObject["connect_urls"]?.jsonArray?.map {
                val items = it.jsonPrimitive.content.split(':')

                NetworkAddress.Immutable(
                    host = items[0],
                    port = items[1].toInt()
                )
            } ?: emptyList()
        )
    } catch (e: Throwable) {
        throw RuntimeException("Can't parse info message. Message: [$json]", e)
    }
}