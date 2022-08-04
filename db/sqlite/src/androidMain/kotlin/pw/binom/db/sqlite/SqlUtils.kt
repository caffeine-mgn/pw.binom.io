package pw.binom.db.sqlite

object SqlUtils {
    fun splitQueryStatements(query: String): List<String> {
        val result = ArrayList<String>()
        var cursor = 0
        var start = 0
        var endChar = '"'
        var isString = false
        while (cursor < query.length) {
            val char = query[cursor]
            if (isString) {
                when (char) {
                    '\\' -> cursor++
                    endChar -> isString = false
                }
            } else {
                when (char) {
                    '[' -> {
                        endChar = ']'
                        isString = true
                    }
                    '\'', '"' -> {
                        endChar = char
                        isString = true
                    }
                    ';' -> {
                        val singleQuery = query.substring(start, cursor).trim()
                        if (singleQuery.isNotEmpty()) {
                            result += singleQuery
                        }
                        start = cursor + 1
                    }
                }
            }
            cursor++
        }
        if (start != query.length) {
            val singleQuery = query.substring(start).trim()
            if (singleQuery.isNotEmpty()) {
                result += singleQuery
            }
        }
        return result
    }
}
