package pw.binom.db.serialization

import pw.binom.collections.defaultHashMap
import pw.binom.date.DateTime
import pw.binom.db.SQLException

class SQLQueryNamedArguments private constructor(
    val sql: String,
    val params: Map<String, List<Int>>,
    val count: Int,
) {
    var lastUsaged: Long = DateTime.nowTime
    fun buildArguments(vararg args: Pair<String, Any?>): Array<Any?> {
        val out = arrayOfNulls<Any>(count)
        args.forEach { arg ->
            params[arg.first]?.forEach {
                out[it] = arg.second
            }
        }
        return out
    }

    companion object {
        /**
         * Returns result sql and map of arguments.
         * Map contains name of argument associated param indexes
         * Result SQL is [sql] with replaced argument to "?"
         */
        fun parse(startQuote: String, endQuote: String, sql: String): SQLQueryNamedArguments {
            var cur = 0
            val sb = StringBuilder()
            var str = false
            val params = defaultHashMap<String, ArrayList<Int>>()
            var paramCount = 0
            var columnNameStarted = false
            var columnOpenPosition = -1
            while (cur < sql.length) {
                if (sql[cur] == '\\') {
                    sb.append('\\')
                    sb.append(sql[cur + 1])
                    cur += 2
                    continue
                }
                if (sql[cur] == '"') {
                    if (columnNameStarted) {
                        sb.append(endQuote)
                    } else {
                        columnOpenPosition = cur
                        sb.append(startQuote)
                    }
                    cur++
                    columnNameStarted = !columnNameStarted
                    continue
                }
                if (sql[cur] == '?') {
                    paramCount++
                    sb.append("?")
                    cur++
                    continue
                }
                if (!str && (sql[cur] == ':' || sql[cur] == '$')) {
                    val start = cur
                    cur++
                    while (cur < sql.length) {
                        val varNameChar =
                            sql[cur] in 'a'..'z' || sql[cur] in 'A'..'Z' || sql[cur] in '0'..'9' || sql[cur] == '.' || sql[cur] == '_'
                        if (!varNameChar) {
                            break
                        }
                        cur++
                    }
                    sb.append("?")
                    val varName = sql.substring(start + 1, cur)
                    params.getOrPut(varName) { ArrayList() }.add(paramCount)
                    paramCount++
                    continue
                }
                val q = sql[cur] == '\'' || sql[cur] == '"' || sql[cur] == '`'
                sb.append(sql[cur])

                if (q) {
                    str = !str
                }
                cur++
            }
            if (columnNameStarted) {
                val str = sql.substring(columnOpenPosition)
                throw SQLException("Invalid SQL: expected end of column name. Colum name started near $str")
            }
            return SQLQueryNamedArguments(
                sql = sb.toString(),
                params = params,
                count = paramCount
            )
        }
    }
}
