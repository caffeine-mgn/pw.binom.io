package pw.binom.db.postgresql.async

import pw.binom.date.parseIso8601DateTime

// 0001-12-11 12:00:00 BC
internal object DateUtils {
    fun parseDateTime(str: String) =
        str.removeSuffix(" BC")
            .parseIso8601DateTime(0)
            ?: throw IllegalArgumentException("Can't parse \"$str\" to date")
}
