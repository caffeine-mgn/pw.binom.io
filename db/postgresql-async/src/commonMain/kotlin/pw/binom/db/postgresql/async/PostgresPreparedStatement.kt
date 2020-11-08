package pw.binom.db.postgresql.async

import pw.binom.db.AsyncPreparedStatement
import pw.binom.db.ResultSet
import pw.binom.uuid
import kotlin.random.Random

class PostgresPreparedStatement(val query: String, override val connection: PGConnection) : AsyncPreparedStatement {
    internal var parsed = false
    private val id = Random.uuid()
    private val realQuery: String
    private val paramCount: Int

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
    }

    private val params = arrayOfNulls<Any>(paramCount)

    override fun set(index: Int, value: Float) {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, value: Int) {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, value: Long) {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, value: String) {
        params[index] = value
    }

    override fun set(index: Int, value: Boolean) {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, value: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun setNull(index: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun executeQuery(): ResultSet {
        execute()
        TODO("Not yet implemented")
    }

    override suspend fun executeUpdate() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    suspend fun execute() {
        if (!parsed) {


            val rr = connection.sendRecive(
                PreparedStatementOpeningMessage(
                    statementId = id.toString(),
                    query = realQuery,
                    valuesTypes = listOf(ColumnTypes.Text)
                )
            )
            check(rr is ReadyForQueryMessage)
            println("!!! $rr")
        }
    }
}