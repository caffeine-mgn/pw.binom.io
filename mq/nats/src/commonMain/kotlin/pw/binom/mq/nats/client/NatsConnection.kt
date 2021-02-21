package pw.binom.mq.nats.client

import kotlinx.serialization.json.*
import pw.binom.UUID
import pw.binom.async2
import pw.binom.io.*
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher

class NatsConnection(
    val address: NetworkAddress,
    val dispatcher: NetworkDispatcher,
    val tlsRequired: Boolean,
    val lang: String,
    val echo: Boolean,
    val clientName: String?,
    val user: String?,
    val natsConnector: NatsConnector,
) {
    var serverId: String? = null
        private set
    var serverName: String? = null
        private set
    var maxPayload: Long? = null
        private set
    var cluster: List<NetworkAddress>? = null
        private set

    private var read: AsyncBufferedAsciiInputReader? = null
    private var write: AsyncBufferedAsciiWriter? = null
    private var con: AsyncChannel? = null
    val connected
        get() = con != null

    private suspend fun parseInfoMsg(msg: String) {
        if (!msg.startsWith("INFO ")) {
            throw RuntimeException("Unknown message. Message: [$msg]")
        }
        val json = msg.removePrefix("INFO ")
        try {
            val data = Json.parseToJsonElement(json)
            serverId = data.jsonObject["server_id"]?.jsonPrimitive?.content
            serverName = data.jsonObject["server_name"]?.jsonPrimitive?.content
//            con.client_id = data.jsonObject["client_id"]?.jsonPrimitive?.intOrNull
            maxPayload = data.jsonObject["max_payload"]?.jsonPrimitive?.longOrNull
//            con.proto = data.jsonObject["proto"]?.jsonPrimitive?.intOrNull

            cluster = data.jsonObject["connect_urls"]?.jsonArray?.map {
                val items = it.jsonPrimitive.content.split(':')
                NetworkAddress.Immutable(
                    host = items[0],
                    port = items[1].toInt()
                )
            }
        } catch (e: Throwable) {
            throw RuntimeException("Can't parse info message. Message: [$json]", e)
        }
    }

    private suspend fun connected(
        con: AsyncChannel,
        read: AsyncBufferedAsciiInputReader,
        write: AsyncBufferedAsciiWriter
    ) {
        this.con = con
        this.read = read
        this.write = write
        natsConnector.connected(this)
    }

    fun connect() {
        async2 {
            val connection = dispatcher.tcpConnect(address)
            val msg = connection.bufferedAsciiWriter()
            val read = connection.bufferedAsciiReader()
            msg.append("CONNECT {")
            msg
                .append("\"verbose\": false, \"tls_required\":").append(tlsRequired)
                .append(", \"lang\":\"").append(lang)
                .append("\",\"version\":\"0.1.28\",\"protocol\":1,\"pedantic\":false")
                .append(",\"echo\":").append(echo)

            if (clientName != null) {
                msg.append(",\"name\":\"").append(clientName).append("\"")
            }
            if (user != null) {
                msg.append(",\"user\":\"").append(user).append("\"")
            }

            msg.append("}\r\n")
            msg.flush()
            val connectMsg = read.readln() ?: throw IOException("Can't connect to Nats")
            println("msg: $connectMsg")
            parseInfoMsg(connectMsg)
            connected(connection, read, msg)
        }
    }

    suspend fun subscribe(subscribeId: UUID, subject: String, group: String?) {
        val app = write!!
        app.append("SUB ").append(subject)
        if (group != null) {
            app.append(" ").append(group)
        }
        app.append(" ").append(subscribeId.toString()).append("\r\n")
        app.flush()
    }
}