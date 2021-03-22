package pw.binom.db.postgresql.async

import pw.binom.UUID
import pw.binom.date.Date
import pw.binom.db.AsyncPreparedStatement
import pw.binom.db.AsyncResultSet
import pw.binom.db.ResultSet
import pw.binom.db.SQLException
import pw.binom.db.postgresql.async.messages.backend.*
import pw.binom.db.postgresql.async.messages.frontend.*
import pw.binom.uuid
import kotlin.random.Random

class PostgresPreparedStatement(
    val query: String,
    override val connection: PGConnection,
    val paramColumnTypes: List<ResultSet.ColumnType>,
    val resultColumnTypes: List<ResultSet.ColumnType>,
) : AsyncPreparedStatement {
    internal var parsed = false
    private val id = Random.uuid()
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
        paramCount = params

        if (paramColumnTypes.isNotEmpty()) {
            require(paramCount == paramColumnTypes.size) { "Invalid count of column type list" }
        }
    }

    private val params = arrayOfNulls<Any>(paramCount)

    override fun set(index: Int, value: Float) {
        params[index] = value
    }

    override fun set(index: Int, value: Int) {
        params[index] = value
    }

    override fun set(index: Int, value: Long) {
        params[index] = value
    }

    override fun set(index: Int, value: String) {
        params[index] = value
    }

    override fun set(index: Int, value: Boolean) {
        params[index] = value
    }

    override fun set(index: Int, value: ByteArray) {
        params[index] = value
    }

    override fun set(index: Int, value: Date) {
        params[index] = value
    }

    override fun setNull(index: Int) {
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

    override fun set(index: Int, value: UUID) {
        params[index] = value
    }

    override suspend fun asyncClose() {
        checkClosePreviousResultSet()
        connection.prepareStatements.remove(this)
        connection.sendOnly(connection.reader.closeMessage.also {
            it.portal = false
            it.statement = id.toString()
        })
        connection.sendOnly(SyncMessage)
        val closeMsg = connection.readDesponse()
        check(closeMsg is CloseCompleteMessage) { "Expected CloseCompleteMessage, but actual $closeMsg" }
        val readyForQuery = connection.readDesponse()
        check(readyForQuery is ReadyForQueryMessage) { "Expected ReadyForQueryMessage, but actual $readyForQuery" }
    }

    suspend fun execute(): QueryResponse {
        val types = if (paramColumnTypes.isEmpty()) {
            emptyList()
        } else {
            paramColumnTypes.map { it.typeInt }
        }
        if (!parsed) {
            connection.sendOnly(
                connection.reader.preparedStatementOpeningMessage.also {
                    it.statementId = id.toString()
                    it.query = realQuery
                    it.valuesTypes = types
                }
            )
        }
        val binaryResult = true
        val portalName = id.toString()
        connection.sendOnly(connection.reader.bindMessage.also {
            it.statement = id.toString()
            it.portal = portalName
            it.values = params
            it.valuesTypes = types
            it.binaryResult = binaryResult
        })
        connection.sendOnly(connection.reader.describeMessage.also {
            it.statement = id.toString()
            it.portal = true
        })
        connection.sendOnly(connection.reader.executeMessage.also {
            it.statementId = portalName
            it.limit = 0
        })
        connection.sendOnly(SyncMessage)
        if (!parsed) {
            val msg = connection.readDesponse()
            if (msg is ErrorMessage) {
                check(connection.readDesponse() is ReadyForQueryMessage)
                throw PostgresqlException(msg.toString())
            }
            check(msg is ParseCompleteMessage) { "Invalid Message: $msg (${msg::class})" }
        }
        val msg = connection.readDesponse()
        when (msg) {
            is ErrorMessage -> {
                check(connection.readDesponse() is ReadyForQueryMessage)
                throw PostgresqlException(msg.toString())
            }
            is BindCompleteMessage -> {
                //ok
            }
        }
        parsed = true

        var rowsAffected = 0L
        LOOP@ while (true) {
            val msg = connection.readDesponse()
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
                    connection.busy = true
                    val msg2 = connection.reader.data
                    msg2.reset(msg)
                    return msg2
                }
                is ErrorMessage -> {
                    throw PostgresqlException(msg.fields['M'])
                }
                is NoticeMessage -> {
                    continue@LOOP
                }
                is NoDataMessage -> {
                    continue@LOOP
                }
                else -> throw SQLException("Unexpected Message. Response Type: [${msg::class}], Message: [$msg]")
            }
        }
    }
}