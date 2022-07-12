package pw.binom.db.postgresql.async

import pw.binom.date.parseIso8601Date

// 0001-12-11 12:00:00 BC
internal object DateUtils {
    fun parseDate(str: String) =
        str.removeSuffix(" BC")
            .parseIso8601Date(0)
            ?: throw IllegalArgumentException("Can't parse \"$str\" to date")
}
