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
        val o = ByteArrayOutput()
        val pool = ByteBufferPool(10)
        val buf = ByteBuffer.alloc(8)
        o.writeInt(buf, 0)
        o.writeShort(buf, 3)
        o.writeShort(buf, 0)
        val appender = BufferedOutputAppendable(Charsets.UTF8, o, pool)
        properties.forEach {
            appender.append(it.key)
            appender.flush()
            o.writeByte(buf, 0)
            appender.append(it.value)
            appender.flush()
            o.writeByte(buf, 0)
        }
        o.writeByte(buf, 0)

        val pos = o.data.position
        o.data.position = 0
        o.data.writeInt(buf, (o.size))
        o.data.position = pos
        o.data.flip()
        connection.write(o.data)
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