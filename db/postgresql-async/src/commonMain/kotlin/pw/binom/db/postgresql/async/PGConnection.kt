package pw.binom.db.postgresql.async

import kotlinx.coroutines.Dispatchers
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.charset.Charset
import pw.binom.charset.CharsetCoder
import pw.binom.charset.Charsets
import pw.binom.collections.defaultMutableSet
import pw.binom.db.ColumnType
import pw.binom.db.SQLException
import pw.binom.db.TransactionMode
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.backend.*
import pw.binom.db.postgresql.async.messages.frontend.CredentialMessage
import pw.binom.db.postgresql.async.messages.frontend.SASLInitialResponse
import pw.binom.db.postgresql.async.messages.frontend.SASLResponse
import pw.binom.db.postgresql.async.messages.frontend.Terminate
import pw.binom.io.*
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.network.*
import pw.binom.scram.ScramSaslClient
import pw.binom.writeByte
import pw.binom.writeInt
import pw.binom.writeShort

private var counter = 0

class PGConnection private constructor(
    val connection: TcpConnection,
    charset: Charset,
    val userName: String,
    val password: String?,
    val networkDispatcher: NetworkManager,
    val temporalBufferSize: Int = DEFAULT_BUFFER_SIZE,
    readBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : AsyncConnection {
    internal var busy = false
    private val id = counter++
    private var closing = false
    internal var prepareStatements = defaultMutableSet<PostgresPreparedStatement>()

    companion object {
        const val TYPE = "PostgreSQL"
        suspend fun connect(
            address: InetNetworkAddress,
            applicationName: String? = "Binom Async Client",
            networkDispatcher: NetworkManager = Dispatchers.Network,
            userName: String,
            password: String,
            dataBase: String,
            charset: Charset = Charsets.UTF8,
            readBufferSize: Int = DEFAULT_BUFFER_SIZE,
        ): PGConnection {
            val connection = networkDispatcher.tcpConnect(address)
            try {
                val pgConnection = PGConnection(
                    connection = connection,
                    charset = charset,
                    userName = userName,
                    password = password,
                    networkDispatcher = networkDispatcher,
                    readBufferSize = readBufferSize,
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
                    val msg = pgConnection.readResponse()
                    if (msg is ErrorMessage) {
                        throw PostgresqlException(msg.toString())
                    }
                    if (msg is ReadyForQueryMessage) {
                        break
                    }
                }
                if (applicationName != null) {
                    val appName = applicationName.replace("\\", "\\\\").replace("'", "\\'")
                    pgConnection.sendQuery("SET application_name = E'$appName'")
                }
                while (true) {
                    val msg = pgConnection.readResponse()
                    if (msg is ReadyForQueryMessage) {
                        break
                    }
                }
//            pgConnection.query("SET AUTOCOMMIT = OFF")
                return pgConnection
            } catch (e: Throwable) {
                connection.closeAnyway()
                throw e
            }
        }
    }

    init {
        PgMetrics.pgConnections.inc()
    }

    private val temporalBuffer = ByteBuffer(temporalBufferSize)
    private val packageWriter = PackageWriter(this, temporalBuffer = temporalBuffer)
    internal val charsetUtils = CharsetCoder(charset)
    private var connected = true
    override val isConnected
        get() = connected
    override val dbInfo: DatabaseInfo
        get() = PostgreSQLDatabaseInfo
    private var closed = false
    private fun checkClosed() {
        if (closed) {
            throw ClosedException()
        }
    }

    private var transactionStarted = false
    private var _transactionMode: TransactionMode = TransactionMode.READ_COMMITTED

    override val transactionMode: TransactionMode
        get() = _transactionMode

    private val packageReader = connection.bufferedAsciiReader(closeParent = false, bufferSize = readBufferSize)
    internal val reader = PackageReader(
        connection = this,
        charset = charset,
        rawInput = packageReader,
        temporalBuffer = temporalBuffer,
    )
    private var credentialMessage = CredentialMessage()
    private var saslInitialResponse = SASLInitialResponse()
    override val type: String
        get() = TYPE

    internal suspend fun sendQuery(query: String): KindedMessage {
        check(!busy) { "Connection is busy" }
        val msg = this.reader.queryMessage
        msg.query = query
        return sendReceive(msg)
    }

    private inline fun <T> busy(f: () -> T): T {
        check(!busy) { "Connection are busy" }
        busy = true
        try {
            return f()
        } finally {
            busy = false
        }
    }

    internal suspend fun query(query: String): QueryResponse {
        val msg = this.reader.queryMessage
        msg.query = query
        sendOnly(msg)
        var statusMsg: String? = null
        var rowsAffected = 0L
        LOOP@ while (true) {
            when (val msg2 = readResponse()) {
                is ReadyForQueryMessage -> return QueryResponse.Status(
                    status = statusMsg ?: "",
                    rowsAffected = rowsAffected
                )

                is CommandCompleteMessage -> {
                    statusMsg = msg2.statusMessage
                    rowsAffected += msg2.rowsAffected
                    continue@LOOP
                }

                is RowDescriptionMessage -> {
                    busy = true
                    val msg3 = reader.data
                    msg3.reset(msg2)
                    return msg3
                }

                is ErrorMessage -> {
                    checkType<ReadyForQueryMessage>(readResponse())
                    throw PostgresqlException("${msg2.fields['M']}. Query: $query")
                }

                is NoticeMessage -> continue@LOOP
                is NoDataMessage -> continue@LOOP
                else -> throw SQLException("Unexpected Message. Response Type: [${msg2::class}], Message: [$msg2], Query: [$query]")
            }
        }
    }

    internal suspend fun sendOnly(msg: KindedMessage) {
        if (closed) {
            throw ClosedException()
        }
        msg.write(packageWriter)
        packageWriter.finishAsync(connection)
        connection.flush()
    }

    private suspend fun sendReceive(msg: KindedMessage): KindedMessage {
        try {
            sendOnly(msg)
            return readResponse()
        } catch (e: SocketClosedException) {
            connected = false
            throw e
        }
    }

    internal suspend fun readResponse(): KindedMessage = KindedMessage.read(reader)

    private suspend fun request(msg: KindedMessage): KindedMessage {
        msg.write(packageWriter)
        packageWriter.finishAsync(connection)
        return readResponse()
    }

    private suspend fun oneShotAuth(authRequest: KindedMessage) {
        when (val msg2 = request(authRequest)) {
            is ErrorMessage -> throw IOException(msg2.fields['M'] ?: msg2.fields['R'] ?: "Error")
            is AuthenticationMessage.AuthenticationOkMessage -> {
                return
            }

            else -> throw SQLException("Unexpected Message. Message: [$msg2]")
        }
    }

    private suspend fun saslAuth(algorithms: List<String>) {
        val scramSaslClient = ScramSaslClient.create(algorithms)
        val msg = scramSaslClient.start(username = userName, password = password)
        saslInitialResponse.mechanism = scramSaslClient.mechanism
        saslInitialResponse.slasData = msg.encodeToByteArray()
        var resp = request(saslInitialResponse)
        while (true) {
            when (resp) {
                is AuthenticationMessage.SaslContinue -> {
                    val d = scramSaslClient.exchange(resp.data.decodeToString())
                    if (d == null) {
                        if (scramSaslClient.state != ScramSaslClient.State.ENDED) {
                            throw IllegalStateException("Invalid SASL state ${scramSaslClient.state}")
                        }
                        break
                    }
                    val resp1 = SASLResponse(d.encodeToByteArray())
                    resp = request(resp1)
                }

                is AuthenticationMessage.SaslFinal -> {
                    val scramResponse = scramSaslClient.exchange(resp.data.decodeToString())
                    if (scramResponse != null) {
                        throw IllegalStateException("Invalid SASL state. ScramClient should send data")
                    }
                    if (scramSaslClient.state != ScramSaslClient.State.ENDED) {
                        throw IllegalStateException("Invalid SASL state ${scramSaslClient.state}")
                    }
                    checkType<AuthenticationMessage.AuthenticationOkMessage>(readResponse())
                    break
                }

                else -> TODO("Unsupported $resp")
            }
        }
    }

    private suspend fun sendFirstMessage(properties: Map<String, String>) {
        ByteArrayOutput().use { buf2 ->
            buf2.bufferedAsciiWriter(closeParent = false).use { o ->
                ByteBuffer(8).use { buf ->
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
                }
                buf2.locked {
                    connection.write(it)
                }
                connection.flush()
            }
        }
        val msg = readResponse()
        when (msg) {
            is AuthenticationMessage.AuthenticationChallengeCleartextMessage -> {
                credentialMessage.username = userName
                credentialMessage.password = password
                credentialMessage.salt = null
                credentialMessage.authenticationType =
                    AuthenticationMessage.AuthenticationChallengeMessage.AuthenticationResponseType.Cleartext
                oneShotAuth(credentialMessage)
            }

            is AuthenticationMessage.AuthenticationChallengeMessage -> {
                credentialMessage.username = userName
                credentialMessage.password = password
                credentialMessage.salt = msg.salt
                credentialMessage.authenticationType = msg.challengeType
                oneShotAuth(credentialMessage)
            }

            is AuthenticationMessage.SaslAuth -> {
                saslAuth(msg.algorithms)
            }

            is AuthenticationMessage.AuthenticationOkMessage -> null
            is ErrorMessage -> throw IOException(msg.fields['M'] ?: msg.fields['R'] ?: "Error")
            else -> throw RuntimeException("Unknown message type [${msg::class}]")
        }
    }

    private fun checkTransactionStarted() {
        check(transactionStarted) { "Transaction not started" }
    }

    override suspend fun setTransactionMode(mode: TransactionMode) {
        busy {
            if (transactionStarted) {
                query("SET TRANSACTION ${mode.pg}")
            }
            _transactionMode = mode
        }
    }

    override suspend fun createStatement() =
        PostgreAsyncStatement(this)

    override suspend fun prepareStatement(query: String): AsyncPreparedStatement =
        prepareStatement(query, emptyList(), emptyList())

    override fun isReadyForQuery(): Boolean =
        isConnected && !busy

    override suspend fun beginTransaction() {
        busy {
            check(!transactionStarted) { "Transaction already started" }
            val q = query("begin TRANSACTION ISOLATION LEVEL ${transactionMode.pg}")
            transactionStarted = true
            if (q !is QueryResponse.Status) {
                throw SQLException("Invalid response. Excepted QueryResponse.Status, but got $q")
            }
            if (q.status != "BEGIN") {
                throw SQLException("Invalid response. Excepted Status \"BEGIN\", but got ${q.status}")
            }
        }
    }

    fun prepareStatement(
        query: String,
        paramColumnTypes: List<ColumnType>,
        resultColumnTypes: List<ColumnType> = emptyList(),
    ): AsyncPreparedStatement = busy {
        val pst = PostgresPreparedStatement(
            query = query,
            connection = this,
            paramColumnTypes = paramColumnTypes,
            resultColumnTypes = resultColumnTypes
        )
        prepareStatements.add(pst)
        pst
    }

    override fun toString(): String = "PGConnection-$id"

    override suspend fun commit() {
        busy {
            checkTransactionStarted()
            val q = query("commit")
            transactionStarted = false
            if (q !is QueryResponse.Status) {
                throw SQLException("Invalid response. Excepted QueryResponse.Status, but got $q")
            }
            if (q.status != "COMMIT") {
                throw SQLException("Invalid response. Excepted Status \"COMMIT\", but got ${q.status}")
            }
        }
    }

    override suspend fun rollback() {
        busy {
            checkTransactionStarted()
            val q = query("rollback")
            if (q !is QueryResponse.Status) {
                throw SQLException("Invalid response. Excepted QueryResponse.Status, but got $q")
            }
            if (q.status != "ROLLBACK") {
                throw SQLException("Invalid response. Excepted Status \"ROLLBACK\", but got ${q.status}")
            }
            transactionStarted = false
        }
    }

    override suspend fun asyncClose() {
        checkClosed()
        if (closing) {
            return
        }
        check(!busy) { "Connection busy" }
        closing = true
        try {
            prepareStatements.toTypedArray().forEach {
                it.asyncCloseAnyway()
            }
            prepareStatements.clear()
        } finally {
            PgMetrics.pgConnections.dec()
            runCatching { sendOnly(Terminate()) }
            reader.closeAnyway()
            connection.asyncCloseAnyway()
            closed = true
            connected = false
            try {
                Closeable.close(charsetUtils, packageWriter, temporalBuffer)
            } finally {
                packageReader.asyncCloseAnyway()
            }
        }
    }
}
