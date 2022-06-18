package pw.binom.db.postgresql.async

// import com.ionspin.kotlin.bignum.decimal.BigDecimal
// import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.UUID
import pw.binom.date.Date
import pw.binom.db.ResultSet
import pw.binom.db.SQLException
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.postgresql.async.messages.backend.*
import pw.binom.db.postgresql.async.messages.frontend.SyncMessage
import pw.binom.nextUuid
import kotlin.random.Random

class PostgresPreparedStatement(
    val query: String,
    override val connection: PGConnection,
    val paramColumnTypes: List<ResultSet.ColumnType>,
    val resultColumnTypes: List<ResultSet.ColumnType>,
) : AsyncPreparedStatement {
    internal var parsed = false
    private val id = Random.nextUuid().toString()
    private val realQuery: String
    private val paramCount: Int
    internal var lastOpenResultSet: PostgresAsyncResultSet? = null

    private suspend fun checkClosePreviousResultSet() {
        lastOpenResultSet?.let { if (!it.isClosed) it.asyncClose() }
        lastOpenResultSet = null
    }

    init {
        val result = StringBuilder(query.length + 16)
        var offset = 0
        var params = 0
        while (offset < query.length) {
            val next = query.indexOf('?', offset)
            if (next == -1) {
                result.append(query.substring(offset))
                offset = query.length
            } else {
                result.append(query.substring(offset, next))
                offset = next + 1
                if (offset < query.length && query[offset] == '?') {
                    result.append('?')
                    offset += 1
                } else {
                    result.append('$')
                    params += 1
                    result.append(params.toString())
                }
            }
        }
        realQuery = result.toString()
        params = 0
        var i = -1
        while (true) {
            i = realQuery.indexOf('$', i + 1)
            if (i == -1) {
                break
            }
            params++
        }
        paramCount = params

        if (paramColumnTypes.isNotEmpty()) {
            require(paramCount == paramColumnTypes.size) { "Invalid count of column type list" }
        }
    }

    private val params = arrayOfNulls<Any>(paramCount)
//    override suspend fun set(index: Int, value: BigInteger) {
//        params[index] = value
//    }
//
//    override suspend fun set(index: Int, value: BigDecimal) {
//        params[index] = value
//    }

    override suspend fun set(index: Int, value: Double) {
        params[index] = value
    }

    override suspend fun set(index: Int, value: Float) {
        params[index] = value
    }

    override suspend fun set(index: Int, value: Int) {
        params[index] = value
    }

    override suspend fun set(index: Int, value: Long) {
        params[index] = value
    }

    override suspend fun set(index: Int, value: String) {
        params[index] = value
    }

    override suspend fun set(index: Int, value: Boolean) {
        params[index] = value
    }

    override suspend fun set(index: Int, value: ByteArray) {
        params[index] = value
    }

    override suspend fun set(index: Int, value: Date) {
        params[index] = value
    }

    override suspend fun setNull(index: Int) {
        params[index] = null
    }

    override suspend fun executeQuery(): AsyncResultSet {
        checkClosePreviousResultSet()
        val response = execute()
        if (response is QueryResponse.Data) {
            val q = PostgresAsyncResultSet(true, response)
            lastOpenResultSet = q
            return q
        }
        throw SQLException("Query doesn't return data")
    }

    override suspend fun executeUpdate(): Long {
        checkClosePreviousResultSet()
        val response = execute()
        if (response is QueryResponse.Status) {
            return response.rowsAffected
        }
        if (response is QueryResponse.Data) {
            response.asyncClose()
        }
        throw SQLException("Query returns data")
    }

    override suspend fun set(index: Int, value: UUID) {
        params[index] = value
    }

    private suspend fun deleteSelf(isPortal: Boolean) {
        connection.sendOnly(
            connection.reader.closeMessage.also {
                it.portal = isPortal
                it.statement = id
            }
        )
        connection.sendOnly(SyncMessage)
        val closeMsg = connection.readDesponse()
        check(closeMsg is CloseCompleteMessage) { "Expected CloseCompleteMessage, but actual $closeMsg" }
        val readyForQuery = connection.readDesponse()
        check(readyForQuery is ReadyForQueryMessage) { "Expected ReadyForQueryMessage, but actual $readyForQuery" }
    }

    override suspend fun asyncClose() {
        checkClosePreviousResultSet()
        connection.prepareStatements.remove(this)
        deleteSelf(isPortal = false)
    }

    suspend fun execute(): QueryResponse {
        val types = if (paramColumnTypes.isEmpty()) {
            emptyList()
        } else {
            paramColumnTypes.map { it.typeInt }
        }
        var justParsed = false
        if (!parsed) {
            connection.sendOnly(
                connection.reader.preparedStatementOpeningMessage.also {
                    it.statementId = id
                    it.query = realQuery
                    it.valuesTypes = types
                }
            )
            justParsed = true
        }
        val binaryResult = false
        connection.sendOnly(
            connection.reader.bindMessage.also {
                it.statement = id
                it.portal = id
                it.values = params
                it.valuesTypes = types
                it.binaryResult = binaryResult
            }
        )
        connection.sendOnly(
            connection.reader.describeMessage.also {
                it.statement = id
                it.portal = true
            }
        )
        connection.sendOnly(
            connection.reader.executeMessage.also {
                it.statementId = id
                it.limit = 0
            }
        )
        connection.sendOnly(SyncMessage)
        if (!parsed) {
            val msg = connection.readDesponse()
            if (msg is ErrorMessage) {
                check(connection.readDesponse() is ReadyForQueryMessage)
                throw PostgresqlException("$msg. Query: $realQuery")
            }
            check(msg is ParseCompleteMessage) { "Invalid Message: $msg (${msg::class})" }
        }
        val msg = connection.readDesponse()
        when (msg) {
            is ErrorMessage -> {
                try {
                    check(connection.readDesponse() is ReadyForQueryMessage)
                } finally {
                    deleteSelf(isPortal = true)
                    if (justParsed) {
                        deleteSelf(isPortal = false)
                    }
                }
                throw PostgresqlException("$msg. Query: $realQuery")
            }
            is BindCompleteMessage -> {
                // ok
            }
        }
        parsed = true

        var rowsAffected = 0L
        LOOP@ while (true) {
            val msg2 = connection.readDesponse()
            var status = ""
            when (msg2) {
                is ReadyForQueryMessage -> {
                    deleteSelf(isPortal = true)
                    return QueryResponse.Status(
                        status = status,
                        rowsAffected = rowsAffected
                    )
                }
                is CommandCompleteMessage -> {
                    status = msg2.statusMessage
                    rowsAffected += msg2.rowsAffected
                    continue@LOOP
                }
                is RowDescriptionMessage -> {
                    connection.busy = true
                    if (!connection.reader.data.isClosed) {
                        QueryResponse.Data(connection).also {
                            it.reset(msg2)
                            it.portalName = id
                            it.asyncClose()
                        }
                        throw IllegalStateException("Previews rest set not closed")
                    }
                    val msg3 = connection.reader.data
                    msg3.reset(msg2)
                    msg3.portalName = id
                    return msg3
                }
                is ErrorMessage -> {
                    check(connection.readDesponse() is ReadyForQueryMessage)
                    deleteSelf(isPortal = true)
                    throw PostgresqlException("${msg2.fields['M']}. Query: $realQuery")
                }
                is NoticeMessage -> {
                    continue@LOOP
                }
                is NoDataMessage -> {
                    continue@LOOP
                }
                else -> throw SQLException("Unexpected Message. Response Type: [${msg2::class}], Message: [$msg2]")
            }
        }
    }
}
