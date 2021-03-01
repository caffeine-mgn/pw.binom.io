package pw.binom.mq.nats.client

import kotlinx.serialization.json.*
import pw.binom.ByteBuffer
import pw.binom.UUID
import pw.binom.async2
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

    interface Message {
        val connection: NatsRawConnection
        val subject: String
        val sid: UUID
        val replyTo: String?
        val data: ByteBuffer
    }

    private val zeroBuff = ByteBuffer.alloc(0)

    private inner class MessageImpl : Message {
        override val connection: NatsRawConnection
            get() = this@NatsRawConnection
        override var subject: String = ""
        override var sid: UUID = UUID.random()
        override var replyTo: String? = null
        override var data: ByteBuffer = zeroBuff
    }

    private val msg = MessageImpl()
    var isConnected = false
    var isConnecting = true
    private var disconnecting = false
    private val appender = channel.bufferedAsciiWriter()
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
        user: String? = null,
        pass: String? = null,
    ): ConnectInfo {
        check(isConnecting && !isConnected)
        try {
            require((user == null && pass == null) || (user != null && pass != null)) { "Invalid User and Password" }

            val msg = appender
            msg.append("CONNECT {")
            msg
                .append("\"verbose\": false, \"tls_required\":").append(tlsRequired)
                .append(", \"lang\":\"").append(lang)
                .append("\",\"version\":\"0.1.28\",\"protocol\":1,\"pedantic\":false")
                .append(",\"echo\":").append(echo)

            if (clientName != null) {
                msg.append(",\"name\":\"").append(clientName).append("\"")
            }
            if (user != null && pass != null) {
                msg.append(",\"user\":\"").append(user).append("\"")
                    .append(",\"pass\":\"").append(pass).append("\"")
            }



            msg.append("}\r\n")
            appender.flush()
            val connectMsg = reader.readln() ?: throw IOException("Can't connect to Nats")
            val info = parseInfoMsg(connectMsg)
            isConnecting = false
            isConnected = true
            return info
        } catch (e: Throwable) {
            isConnecting = false
            throw e
        }
    }

    suspend fun readMessage(): Message {
        READ_LOOP@ while (true) {
            val msgText = reader.readln() ?: throw SocketClosedException()
            when {
                msgText.startsWith("INFO ") -> parseInfoMsg(msgText)
                msgText == "PING" -> {
                    appender.append("PONG\r\n")
                    appender.flush()
                    continue@READ_LOOP
                }
                msgText.startsWith("MSG ") -> {
                    val items = msgText.split(' ')
                    val size = if (items.size == 5) {
                        items[4].toInt()
                    } else {
                        items[3].toInt()
                    }
                    val data = ByteBuffer.alloc(size)
                    try {
                        reader.readFully(data)
                        reader.readln()
                        msg.subject = items[1]
                        msg.sid = UUID.fromString(items[2])
                        msg.replyTo = if (items.size == 5) {
                            items[3]
                        } else {
                            null
                        }
                        data.flip()
                        msg.data = data
                        return msg
//                        onMessage.invoke(msg)
                    } finally {
                        data.close()
                    }
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
        val app = appender
        app.append("SUB ").append(subject)
        if (group != null) {
            app.append(" ").append(group)
        }
        app.append(" ").append(subscribeId.toString()).append("\r\n")
        app.flush()
    }

    suspend fun unsubscribe(id: UUID, afterMessages: Int? = null) {
        require(afterMessages == null || afterMessages >= 0)
        appender.append("UNSUB ").append(id.toString())
        if (afterMessages != null) {
            appender.append(" ").append(afterMessages.toString())
        }
        appender.append("\r\n")
        appender.flush()
    }

    suspend fun publish(subject: String, replyTo: String? = null, data: ByteBuffer?) {
        require(subject.isNotEmpty() && " " !in subject)
        require(replyTo == null || (replyTo.isNotEmpty() && " " !in replyTo))

        appender.append("PUB ").append(subject)
        if (replyTo != null) {
            appender.append(" ").append(replyTo)
        }
        appender.append(" ").append((data?.remaining ?: 0).toString()).append("\r\n")
        appender.flush()
        if (data != null) {
            while (data.remaining > 0) {
                appender.write(data)
            }
        }
        appender.append("\r\n")
        appender.flush()
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