package pw.binom.db.postgresql.async

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.charset.CharsetCoder
import pw.binom.charset.Charsets
import pw.binom.db.ResultSet
import pw.binom.db.SQLException
import pw.binom.db.TransactionMode
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.backend.*
import pw.binom.db.postgresql.async.messages.frontend.CredentialMessage
import pw.binom.db.postgresql.async.messages.frontend.Terminate
import pw.binom.io.*
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
                    "TimeZone" to "GMT",
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

    private val packageWriter = PackageWriter(this)
    internal val charsetUtils = CharsetCoder(charset)
    private var connected = true
    override val isConnected
        get() = connected
    override val dbInfo: DatabaseInfo
        get() = PostgreSQLDatabaseInfo

    override suspend fun setTransactionMode(mode: TransactionMode) {
        if (transactionStarted) {
            query("SET TRANSACTION ${mode.pg}")
        }
        _transactionMode = mode
    }

    private var transactionStarted = false
    private var _transactionMode: TransactionMode = TransactionMode.READ_COMMITTED

    override val transactionMode: TransactionMode
        get() = _transactionMode

    private val packageReader = connection.bufferedAsciiReader(closeParent = false)
    internal val reader = PackageReader(this, charset, packageReader)
    private var credentialMessage = CredentialMessage()
    override val type: String
        get() = TYPE


    suspend fun sendQuery(query: String): KindedMessage {
        if (busy)
            throw IllegalStateException("Connection is busy")
        val msg = this.reader.queryMessage
        msg.query = query
        return sendRecive(msg)
    }

    suspend fun query(query: String): QueryResponse {
        if (busy)
            throw IllegalStateException("Connection is busy")
        val msg = this.reader.queryMessage
        msg.query = query
        sendOnly(msg)
        var rowsAffected = 0L
        LOOP@ while (true) {
            when (val msg = readDesponse()) {
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
                    throw PostgresqlException("${msg.fields['M']}. Query: $query")
                }
                is NoticeMessage -> {
                    continue@LOOP
                }
                is NoDataMessage -> {
                    continue@LOOP
                }
                else -> throw SQLException("Unexpected Message. Response Type: [${msg::class}], Message: [$msg], Query: [$query]")
            }
        }
    }

    internal suspend fun sendOnly(msg: KindedMessage) {
        msg.write(packageWriter)
        packageWriter.finishAsync(connection)
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


    suspend fun readDesponse(): KindedMessage {
        val msg = KindedMessage.read(reader)
        return msg
    }

    private suspend fun request(msg: KindedMessage): KindedMessage {
        msg.write(packageWriter)
        packageWriter.finishAsync(connection)
        return readDesponse()
    }

    private suspend fun sendFirstMessage(properties: Map<String, String>) {
        ByteArrayOutput().use { buf2 ->
            buf2.bufferedAsciiWriter(closeParent = false).use { o ->
                ByteBuffer.alloc(8) { buf ->
                    o.writeInt(buf, 0)
                    o.writeShort(buf, 3)
                    o.writeShort(buf, 0)
                    properties.forEach {
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
                }
                val wrote = connection.write(buf2.data)
                connection.flush()
            }
        }
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

    override suspend fun createStatement() =
        PostgreAsyncStatement(this)

    override suspend fun prepareStatement(query: String): AsyncPreparedStatement =
        prepareStatement(query, emptyList(), emptyList())

    override fun isReadyForQuery(): Boolean =
        isConnected && !busy

    val TransactionMode.pg
    get()=when (this) {
        TransactionMode.SERIALIZABLE -> "SERIALIZABLE"
        TransactionMode.READ_COMMITTED -> "READ COMMITTED"
        TransactionMode.REPEATABLE_READ -> "REPEATABLE READ"
        TransactionMode.READ_UNCOMMITTED -> "READ UNCOMMITTED"
    }

    override suspend fun beginTransaction() {
        if (transactionStarted) {
            throw IllegalStateException("Transaction already started")
        }
        query("begin TRANSACTION ISOLATION LEVEL ${transactionMode.pg}")
        transactionStarted = true
    }

    internal var prepareStatements = HashSet<PostgresPreparedStatement>()

    fun prepareStatement(
        query: String,
        paramColumnTypes: List<ResultSet.ColumnType>,
        resultColumnTypes: List<ResultSet.ColumnType> = emptyList(),
    ): AsyncPreparedStatement {
        val pst = PostgresPreparedStatement(
            query = query,
            connection = this,
            paramColumnTypes = paramColumnTypes,
            resultColumnTypes = resultColumnTypes
        )
        prepareStatements.add(pst)
        return pst
    }


    override suspend fun commit() {
        if (!transactionStarted) {
            throw IllegalStateException("Transaction not started")
        }
        query("commit")
        transactionStarted = false
    }

    override suspend fun rollback() {
        if (!transactionStarted) {
            throw IllegalStateException("Transaction not started")
        }
        query("rollback")
        transactionStarted = false
    }

    override suspend fun asyncClose() {
        prepareStatements.toTypedArray().forEach {
            it.asyncClose()
        }
        prepareStatements.clear()
        try {
            runCatching { sendOnly(Terminate()) }
            runCatching { reader.close() }
            runCatching { connection.asyncClose() }
        } finally {
            charsetUtils.close()
            packageWriter.close()
            packageReader.asyncClose()
        }
    }
}