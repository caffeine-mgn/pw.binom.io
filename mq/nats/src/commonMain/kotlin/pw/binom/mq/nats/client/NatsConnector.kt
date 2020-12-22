package pw.binom.mq.nats.client

import kotlinx.serialization.json.*
import pw.binom.*
import pw.binom.io.*
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.TcpConnection
import kotlin.random.Random

class NatsConnector(
    val clientName: String? = null,
    val user: String? = null,
    val pass: String? = null,
    val dispatcher: NetworkDispatcher,
    val tlsRequired: Boolean = false,
    val lang: String = "kotlin",
    val echo: Boolean = true,
    val autoConnectToCluster: Boolean = true,
    val messageListener: (suspend (Message) -> Unit)?
) : Closeable {
    interface Message {
        val connection: NatsConnector
        val subject: String
        val sid: UUID
        val replyTo: String?
        val data: ByteBuffer
    }

    private val zeroBuff = ByteBuffer.alloc(0)

    private inner class MessageImpl : Message {
        override val connection: NatsConnector
            get() = this@NatsConnector
        override var subject: String = ""
        override var sid: UUID = UUID.random()
        override var replyTo: String? = null
        override var data: ByteBuffer = zeroBuff
    }

    private val msg = MessageImpl()

    val connections = ArrayList<Connection>()

    init {
        require((user == null && pass == null) || (user != null && pass != null)) {
            "Invalid auth configuration"
        }
    }

    inner class Connection(
        val raw: TcpConnection,
        val appender: AsyncBufferedOutputAppendable,
        val reader: AsyncBufferedAsciiInputReader
    ) {
        var server_id: String? = null
        var server_name: String? = null
        var client_id: Int? = null
        var max_payload: Long? = null
        var proto: Int? = null

        fun start() {
            async {
                try {
                    while (true) {
                        val msg = reader.readln() ?: break
                        when {
                            msg.startsWith("INFO ") -> parseInfoMsg(this, msg)
                            msg == "PING" -> {
                                appender.append("PONG\r\n")
                                appender.flush()
                            }
                            msg.startsWith("MSG ") -> {
                                val items = msg.split(' ')
                                val size = if (items.size == 5) {
                                    items[4].toInt()
                                } else {
                                    items[3].toInt()
                                }
                                val data = ByteBuffer.alloc(size)
                                try {
                                    reader.readFully(data)
                                    reader.readln()
                                    if (messageListener != null) {
                                        this@NatsConnector.msg.subject = items[1]
                                        this@NatsConnector.msg.sid = UUID.fromString(items[2])
                                        this@NatsConnector.msg.replyTo = if (items.size == 5) {
                                            items[3]
                                        } else {
                                            null
                                        }
                                        data.flip()
                                        this@NatsConnector.msg.data = data
                                        messageListener.invoke(this@NatsConnector.msg)
                                    }
                                } finally {
                                    data.close()
                                }
                            }
                            else -> throw IOException("Unknown message type. Message: [$msg]")
                        }
                    }
                } catch (e: Throwable) {
                    connections -= this
                    e.printStackTrace()
                }
            }
        }
    }

    private val pool = ByteBufferPool(10)

    private suspend fun parseInfoMsg(con: Connection, msg: String) {
        if (!msg.startsWith("INFO ")) {
            throw RuntimeException("Unknown message. Message: [$msg]")
        }
        val json = msg.removePrefix("INFO ")
        try {
            val data = Json.parseToJsonElement(json)
            con.server_id = data.jsonObject["server_id"]?.jsonPrimitive?.content
            con.server_name = data.jsonObject["server_name"]?.jsonPrimitive?.content
            con.client_id = data.jsonObject["client_id"]?.jsonPrimitive?.intOrNull
            con.max_payload = data.jsonObject["max_payload"]?.jsonPrimitive?.longOrNull
            con.proto = data.jsonObject["proto"]?.jsonPrimitive?.intOrNull

            if (autoConnectToCluster) {
                data.jsonObject["connect_urls"]?.jsonArray?.forEach {
                    val items = it.jsonPrimitive.content.split(':')
                    connect(
                        NetworkAddress.Immutable(
                            host = items[0],
                            port = items[1].toInt()
                        )
                    )
                }
            }
        } catch (e: Throwable) {
            throw RuntimeException("Can't parse info message. Message: [$json]", e)
        }
    }

    suspend fun connect(address: NetworkAddress) {
        val connection = dispatcher.tcpConnect(address)
        val con = Connection(
            raw = connection,
            appender = connection.bufferedWriter(pool),
            reader = AsyncBufferedAsciiInputReader(connection)
        )

        val msg = StringBuilder("CONNECT {")
        msg
            .append("\"verbose\": false, \"tls_required\":").append(tlsRequired)
            .append(", \"lang\":\"").append(lang)
            .append("\",\"version\":\"0.1.26\",\"protocol\":1,\"pedantic\":false")
            .append(",\"echo\":").append(echo)

        if (clientName != null) {
            msg.append(",\"name\":\"").append(clientName).append("\"")
        }
        if (user != null) {
            msg.append(",\"user\":\"").append(user).append("\"")
        }

        msg.append("}\r\n")
        con.appender.append(msg.toString())
        con.appender.flush()
        parseInfoMsg(con, con.reader.readln() ?: throw IOException("Can't connect to Nats"))
        connections += con
        con.start()
    }

    suspend fun subscribe(subject: String, group: String? = null): UUID {
        require(subject.isNotEmpty() && " " !in subject)
        require(group == null || (group.isNotEmpty() && " " !in group))
        val subscribeId = Random.uuid()
        val app = getConnection().appender
        app.append("SUB ").append(subject)
        if (group != null) {
            app.append(" ").append(group)
        }
        app.append(" ").append(subscribeId.toString()).append("\r\n")
        app.flush()

        return subscribeId
    }

    suspend fun unsubscribe(id: UUID, afterMessages: Int? = null) {
        require(afterMessages == null || afterMessages >= 0)
        val app = getConnection().appender
        app.append("UNSUB ").append(id.toString())
        if (afterMessages != null) {
            app.append(" ").append(afterMessages.toString())
        }
        app.append("\r\n")
        app.flush()
    }

    suspend fun publish(subject: String, replyTo: String? = null, data: ByteBuffer?) {
        require(subject.isNotEmpty() && " " !in subject)
        require(replyTo == null || (replyTo.isNotEmpty() && " " !in replyTo))
        val con = getConnection()
        val app = con.appender

//        val sb = StringBuilder()
        app.append("PUB ").append(subject)
        if (replyTo != null) {
            app.append(" ").append(replyTo)
        }
        app.append(" ").append((data?.remaining ?: 0).toString()).append("\r\n")
        app.flush()
        if (data != null) {
            while (data.remaining > 0) {
                con.raw.write(data)
            }
        }
        con.raw.flush()
        app.append("\r\n")
        app.flush()
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

    private fun getConnection(): Connection =
        connections.getOrNull(0) ?: throw RuntimeException("No connection")

    override fun close() {
        connections.forEach {
            try {
                it.raw.close()
            } catch (e: Throwable) {
                //ignore
            }
        }
    }
}