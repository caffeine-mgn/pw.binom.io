package pw.binom.db.postgresql.async

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.db.*
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.backend.*
import pw.binom.db.postgresql.async.messages.frontend.CredentialMessage
import pw.binom.io.BufferedOutputAppendable
import pw.binom.io.ByteArrayOutput
import pw.binom.io.IOException
import pw.binom.io.bufferedAsciiWriter
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.SocketClosedException
import pw.binom.network.TcpConnection

class PGConnection private constructor(
    val connection: TcpConnection,
    charset: Charset,
    val userName: String,
    val password: String?
) : AsyncConnection {
    internal var busy = false

    companion object {
        const val TYPE = "PostgreSQL"
        suspend fun connect(
            address: NetworkAddress,
            applicationName: String? = "Binom Async Client",
            manager: NetworkDispatcher,
            userName: String,
            password: String,
            dataBase: String,
            charset: Charset = Charsets.UTF8,
        ): PGConnection {
            val connection = manager.tcpConnect(address)
            val pgConnection = PGConnection(
                connection = connection,
                charset = charset,
                userName = userName,
                password = password
            )
            println("First message:")
            pgConnection.sendFirstMessage(
                mapOf(
                    "user" to userName,
                    "database" to dataBase,
                    "client_encoding" to charset.name,
                    "DateStyle" to "ISO",
//                    "extra_float_digits" to "2"
                )
            )
            while (true) {
                val msg = pgConnection.readDesponse()
                println("msg: $msg")
                if (msg is ErrorMessage) {
                    throw PostgresqlException(msg.toString())
                }
                if (msg is ReadyForQueryMessage) {
                    break
                }
            }
            if (applicationName != null) {
                val appName = applicationName?.replace("\\", "\\\\")?.replace("'", "\\'")
                pgConnection.sendQuery("SET application_name = E'$appName'")
            }
            while (true) {
                val msg = pgConnection.readDesponse()
                if (msg is ReadyForQueryMessage) {
                    break
                }
            }
//            pgConnection.query("SET AUTOCOMMIT = OFF")
            return pgConnection
        }
    }

    private val pw = PackageWriter(charset, ByteBufferPool(10))

    private var connected = true
    override val isConnected
        get() = connected

    suspend fun sendQuery(query: String): KindedMessage {
        if (busy)
            throw IllegalStateException("Connection is busy")
        val msg = this.reader.queryMessage
        msg.query = query
        return sendRecive(msg)
    }

    suspend fun query(query: String): QueryResponse {
//        val msg = sendQuery(query)
        if (busy)
            throw IllegalStateException("Connection is busy")
        val msg = this.reader.queryMessage
        msg.query = query
        sendOnly(msg)
        var rowsAffected = 0L
        LOOP@ while (true) {
            val msg = readDesponse()
            when (msg) {
                is ReadyForQueryMessage -> {
                    return QueryResponse.Status(
                        status = "",
                        rowsAffected = rowsAffected
                    )
                }
                is CommandCompleteMessage -> {
                    rowsAffected += msg.rowsAffected
                    continue@LOOP
                }
                is RowDescriptionMessage -> {
                    busy = true
                    val msg2 = reader.data
                    msg2.reset(msg)
                    return msg2
                }
                is ErrorMessage -> {
                    throw PostgresqlException(msg.fields['M'])
                }
                is NoticeMessage -> {
                    continue@LOOP
                }
                else -> throw SQLException("Unexpected Message. Response Type: [${msg::class}], Message: [$msg]")
            }
        }
    }

    internal suspend fun sendOnly(msg: KindedMessage) {
        msg.write(pw)
        pw.finishAsync(connection)
        connection.flush()
    }

    internal suspend fun sendRecive(msg: KindedMessage): KindedMessage {
        try {
            sendOnly(msg)
            return readDesponse()
        } catch (e: SocketClosedException) {
            connected = false
            throw e
        }
    }

    internal val reader = PackageReader(this, charset, connection)
    private var credentialMessage = CredentialMessage()

    suspend fun readDesponse(): KindedMessage {
        val msg = KindedMessage.read(reader)
        return msg
    }

    private suspend fun request(msg: KindedMessage): KindedMessage {
        msg.write(pw)
        pw.finishAsync(connection)
        return readDesponse()
    }

    private suspend fun sendFirstMessage(properties: Map<String, String>) {
        val buf2 = ByteArrayOutput()
        val o = buf2.bufferedAsciiWriter()
        val buf = ByteBuffer.alloc(8)
        o.writeInt(buf, 0)
        o.writeShort(buf, 3)
        o.writeShort(buf, 0)
        properties.forEach {
            println("${it.key}: ${it.value}")
            o.append(it.key)
            o.writeByte(buf, 0)
            o.append(it.value)
            o.writeByte(buf, 0)
        }
        o.writeByte(buf, 0)
        o.flush()

        val pos = buf2.data.position
        buf2.data.position = 0
        buf2.data.writeInt(buf, (buf2.size))
        buf2.data.position = pos
        buf2.data.flip()
        println("Seding data with size $pos")
        buf2.data.forEach {
            print(" ${it.toChar()}")
        }
        connection.write(buf2.data)
        val msg = readDesponse()
        val authRequest = when (msg) {
            is AuthenticationMessage.AuthenticationChallengeCleartextMessage -> {
                credentialMessage.username = userName
                credentialMessage.password = password
                credentialMessage.salt = null
                credentialMessage.authenticationType =
                    AuthenticationMessage.AuthenticationChallengeMessage.AuthenticationResponseType.Cleartext
                credentialMessage
            }
            is AuthenticationMessage.AuthenticationChallengeMessage -> {
                credentialMessage.username = userName
                credentialMessage.password = password
                credentialMessage.salt = msg.salt
                credentialMessage.authenticationType = msg.challengeType
                credentialMessage
            }
            is AuthenticationMessage.AuthenticationOkMessage -> null
            is ErrorMessage -> throw IOException(msg.fields['M'] ?: msg.fields['R'] ?: "Error")
            else -> throw RuntimeException("Unknown message type [${msg::class}]")
        }
        if (authRequest != null) {
            when (val msg = request(authRequest)) {
                is ErrorMessage -> throw IOException(msg.fields['M'] ?: msg.fields['R'] ?: "Error")
                is AuthenticationMessage.AuthenticationOkMessage -> {
                    return
                }
                else -> throw SQLException("Unexpected Message. Message: [$msg]")
            }
        }

    }

    override fun createStatement() =
        PostgreAsyncStatement(this)

    override fun prepareStatement(query: String): AsyncPreparedStatement =
        prepareStatement(query, emptyList(), emptyList())

    fun prepareStatement(
        query: String,
        paramColumnTypes: List<ResultSet.ColumnType>,
        resultColumnTypes: List<ResultSet.ColumnType> = emptyList(),
    ): AsyncPreparedStatement =
        PostgresPreparedStatement(
            query = query,
            connection = this,
            paramColumnTypes = paramColumnTypes,
            resultColumnTypes = resultColumnTypes
        )

    override suspend fun commit() {
        query("commit")
    }

    override suspend fun rollback() {
        query("rollback")
    }

    override val type: String
        get() = TYPE

    override suspend fun asyncClose() {
        connection.asyncClose()
    }
}