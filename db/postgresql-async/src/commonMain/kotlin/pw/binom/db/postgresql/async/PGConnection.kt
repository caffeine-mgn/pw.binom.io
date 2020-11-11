package pw.binom.db.postgresql.async

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.db.AsyncConnection
import pw.binom.db.AsyncPreparedStatement
import pw.binom.db.ResultSet
import pw.binom.db.SyncStatement
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds
import pw.binom.db.postgresql.async.messages.backend.*
import pw.binom.db.postgresql.async.messages.frontend.CredentialMessage
import pw.binom.io.BufferedOutputAppendable
import pw.binom.io.ByteArrayOutput
import pw.binom.io.IOException
import pw.binom.io.socket.nio.SocketNIOManager

class PGConnection private constructor(
    val connection: SocketNIOManager.ConnectionRaw,
    charset: Charset,
    val userName: String,
    val password: String?
) : AsyncConnection {
    internal var busy = false

    companion object {
        const val TYPE = "PostgreSQL"
        suspend fun connect(
            host: String,
            port: Int,
            applicationName: String = "Binom Async Client",
            manager: SocketNIOManager,
            userName: String,
            password: String,
            dataBase: String,
            charset: Charset = Charsets.UTF8,
        ): PGConnection {
            val connection = manager.connect(
                host = host,
                port = port
            )
            val pgConnection = PGConnection(
                connection = connection,
                charset = charset,
                userName = userName,
                password = password
            )
            val appName = applicationName.replace("\\", "\\\\").replace("'", "\\'")
            pgConnection.sendFirstMessage(
                mapOf(
                    "user" to userName,
                    "database" to dataBase,
                    "client_encoding" to charset.name,
                    "DateStyle" to "ISO",
                    "extra_float_digits" to "2"
                )
            )
            while (true) {
                val msg = pgConnection.readDesponse()
                if (msg is ReadyForQueryMessage) {
                    break
                }
            }
            println("Connection done!")
            val mg = pgConnection.sendQuery("SET application_name = E'$appName'")
            while (true) {
                val msg = pgConnection.readDesponse()
                if (msg is ReadyForQueryMessage) {
                    break
                }
            }
            println(mg)
            return pgConnection
        }
    }

    private val pw = PackageWriter(charset, ByteBufferPool(10))

    suspend fun sendQuery(query: String): KindedMessage {
        if (busy)
            throw IllegalStateException("Connection is busy")
        val msg = this.reader.queryMessage
        msg.query = query
        return sendRecive(msg)
    }

    suspend fun query(query: String): QueryResponse {
        val msg = sendQuery(query)
        return when (msg) {
            is CommandCompleteMessage -> {
                check(this.readDesponse() is ReadyForQueryMessage)
                QueryResponse.Status(
                    status = msg.statusMessage,
                    rowsAffected = msg.rowsAffected
                )
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
            else -> TODO("msg: $msg")
        }
    }

    internal suspend fun sendOnly(msg: KindedMessage) {
        print("Send Query $msg...")
        msg.write(pw)
        pw.finishAsync(connection)
        connection.flush()
        println("OK")
    }

    internal suspend fun sendRecive(msg: KindedMessage): KindedMessage {
        sendOnly(msg)
        return readDesponse()
    }

    internal val reader = PackageReader(this, charset, connection)
    private var credentialMessage = CredentialMessage()

    suspend fun readDesponse(): KindedMessage {
        print("Getting message...")
        val msg = KindedMessage.read(reader)
        println("Got: [$msg]")
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
        println("o.size=${o.size}, o.data.remaining=${o.data.remaining}")
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
            else -> TODO()
        }
        if (authRequest != null) {
            when (val msg = request(authRequest)) {
                is ErrorMessage -> throw IOException(msg.fields['M'] ?: msg.fields['R'] ?: "Error")
                is AuthenticationMessage.AuthenticationOkMessage -> {
                    println("Auth Complite!")
                }
            }
        }

    }

    private suspend fun sendFirstMessage1(appName: String) {
        val o = ByteArrayOutput()
        val pool = ByteBufferPool(10)
        val buf = ByteBuffer.alloc(8)
        o.writeByte(buf, MessageKinds.Query)
        o.writeInt(buf, 0)
        val appender = BufferedOutputAppendable(Charsets.UTF8, o, pool)
        appender.append("set application_name=E'$appName'")
        appender.flush()
        o.writeByte(buf, 0)
        val pos = o.data.position
        o.data.position = 1
        o.data.writeInt(buf, (o.size - 1))
        o.data.position = pos
        o.data.flip()
        println("o.size=${o.size}, o.data.remaining=${o.data.remaining}")
        connection.write(o.data)
        readDesponse()
    }

    override fun createStatement(): SyncStatement {
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

    override suspend fun rollback() {
        TODO("Not yet implemented")
    }

    override val type: String
        get() = TYPE

    override suspend fun close() {
        connection.close()
    }
}