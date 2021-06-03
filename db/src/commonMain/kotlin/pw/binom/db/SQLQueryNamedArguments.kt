package pw.binom.db

class SQLQueryNamedArguments private constructor(
    val sql: String,
    val params: Map<String, List<Int>>,
    val count: Int
) {
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
        fun parse(sql: String): SQLQueryNamedArguments {
            var cur = 0
            val sb = StringBuilder()
            var str = false
            val params = HashMap<String, ArrayList<Int>>()
            var paramCount = 0
            while (cur < sql.length) {
                if (sql[cur] == '\\') {
                    sb.append('\\')
                    sb.append(sql[cur + 1])
                    cur += 2
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
            return SQLQueryNamedArguments(
                sql = sb.toString(),
                params = params,
                count = paramCount
            )
        }
    }
}