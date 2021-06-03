package pw.binom.db.async

import pw.binom.db.ResultSet
import pw.binom.db.SQLException
import pw.binom.io.AsyncCloseable
import pw.binom.io.use

interface AsyncResultSet : ResultSet, AsyncCloseable {
    suspend fun next(): Boolean
}

/**
 * Calls [mapper] for each rows of this [AsyncResultSet]. Makes list of result [mapper] and returns it.
 * Closes this [AsyncResultSet] after call [mapper] for each values of this [AsyncResultSet]
 */
suspend inline fun <T> AsyncResultSet.map(mapper: suspend (AsyncResultSet) -> T): List<T> {
    val out = ArrayList<T>()
    use {
        while (next()) {
            out += mapper(this)
        }
    }
    return out
}

suspend inline fun AsyncResultSet.forEach(func: suspend (AsyncResultSet) -> Unit) {
    use {
        while (next()) {
            func(this)
        }
    }
}

/**
 * Returns result of call [mapper] for single value from this [AsyncResultSet]. If this [AsyncResultSet] contains row more than one
 * will throw [SQLException]. If this [AsyncResultSet] is empty will return null.
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.singleOrNull(mapper: (ResultSet) -> T): T? =
    use {
        if (!next()) {
            return@use null
        }
        val result = mapper(this)
        if (next()) {
            throw SQLException("Found more than one results")
        }
        result
    }

/**
 * Returns result of call [mapper] for single value from this [AsyncResultSet]. If this [AsyncResultSet] contains row more than one
 * will throw [SQLException].
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.single(mapper: (ResultSet) -> T): T =
    use {
        if (!next()) {
            throw SQLException("Can't find any value")
        }
        val result = mapper(this)
        if (next()) {
            throw SQLException("Found more than one results")
        }
        result
    }

/**
 * Returns result of call [mapper] for first value from this [AsyncResultSet]. If this [AsyncResultSet] is empty will
 * return null.
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.firstOrNull(mapper: (ResultSet) -> T): T? =
    use {
        if (!next()) {
            return@use null
        }
        mapper(this)
    }

/**
 * Returns result of call [mapper] for first value from this [AsyncResultSet]. If this [AsyncResultSet] is empty will
 * throw [SQLException].
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.first(mapper: (ResultSet) -> T): T? =
    use {
        if (!next()) {
            throw SQLException("Can't find any value")
        }
        mapper(this)
    }