package pw.binom.db.sync

import pw.binom.collections.defaultArrayList
import pw.binom.db.ResultSet
import pw.binom.db.SQLException
import pw.binom.db.async.AsyncResultSet
import pw.binom.io.Closeable
import pw.binom.io.use

interface SyncResultSet : ResultSet, Closeable {
    fun next(): Boolean

    fun <T> map(mapper: (ResultSet) -> T) =
        SyncResultSetIterator(this, mapper)
}

/**
 * Calls [mapper] for each rows of this [AsyncResultSet]. Makes list of result [mapper] and returns it.
 * Closes this [AsyncResultSet] after call [mapper] for each values of this [AsyncResultSet]
 */
suspend inline fun <T> SyncResultSet.map(mapper: (SyncResultSet) -> T): List<T> {
    val out = defaultArrayList<T>()
    use {
        while (next()) {
            out += mapper(this)
        }
    }
    return out
}

inline fun SyncResultSet.forEach(func: (SyncResultSet) -> Unit) {
    while (next()) {
        func(this)
    }
}

inline fun SyncResultSet.read(func: (SyncResultSet) -> Unit) {
    try {
        forEach(func)
    } finally {
        close()
    }
}

/**
 * Returns result of call [mapper] for single value from this [AsyncResultSet]. If this [AsyncResultSet] contains row more than one
 * will throw [SQLException]. If this [AsyncResultSet] is empty will return null.
 * Closes this [AsyncResultSet] after call [mapper]
 */
fun <T> SyncResultSet.singleOrNull(mapper: (ResultSet) -> T): T? =
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
fun <T> SyncResultSet.single(mapper: (ResultSet) -> T): T =
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
fun <T> SyncResultSet.firstOrNull(mapper: (ResultSet) -> T): T? =
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
fun <T> SyncResultSet.first(mapper: (ResultSet) -> T): T? =
    use {
        if (!next()) {
            throw SQLException("Can't find any value")
        }
        mapper(this)
    }
