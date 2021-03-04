package pw.binom.db

import pw.binom.io.AsyncCloseable

interface AsyncResultSet : ResultSet, AsyncCloseable {
    suspend fun next(): Boolean
}

suspend inline fun AsyncResultSet.forEach(func: suspend (AsyncResultSet) -> Unit) {
    try {
        while (next()) {
            func(this)
        }
    } finally {
        asyncClose()
    }
}

/**
 * Returns result of call [mapper] for single value from this [AsyncResultSet]. If this [AsyncResultSet] contains row more than one
 * will throw [SQLException]. If this [AsyncResultSet] is empty will return null.
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.singleOrNull(mapper: (ResultSet) -> T): T? {
    try {
        if (!next()) {
            return null
        }
        val result = mapper(this)
        if (next()) {
            throw SQLException("Found more than one results")
        }
        return result
    } finally {
        asyncClose()
    }
}

/**
 * Returns result of call [mapper] for single value from this [AsyncResultSet]. If this [AsyncResultSet] contains row more than one
 * will throw [SQLException].
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.single(mapper: (ResultSet) -> T): T {
    try {
        if (!next()) {
            throw SQLException("Can't find any value")
        }
        val result = mapper(this)
        if (next()) {
            throw SQLException("Found more than one results")
        }
        return result
    } finally {
        asyncClose()
    }
}

/**
 * Returns result of call [mapper] for first value from this [AsyncResultSet]. If this [AsyncResultSet] is empty will
 * return null.
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.firstOrNull(mapper: (ResultSet) -> T): T? {
    try {
        if (!next()) {
            return null
        }
        return mapper(this)
    } finally {
        asyncClose()
    }
}

/**
 * Returns result of call [mapper] for first value from this [AsyncResultSet]. If this [AsyncResultSet] is empty will
 * throw [SQLException].
 * Closes this [AsyncResultSet] after call [mapper]
 */
suspend fun <T> AsyncResultSet.first(mapper: (ResultSet) -> T): T? {
    try {
        if (!next()) {
            throw SQLException("Can't find any value")
        }
        return mapper(this)
    } finally {
        asyncClose()
    }
}

/**
 * Calls [mapper] for each rows of this [AsyncResultSet]. Makes list of result [mapper] and returns it.
 * Closes this [AsyncResultSet] after call [mapper] for each values of this [AsyncResultSet]
 */
suspend fun <T> AsyncResultSet.list(mapper: (AsyncResultSet) -> T): List<T> {
    try {
        val out = ArrayList<T>()
        while (next()) {
            out += mapper(this)
        }
        return out
    } finally {
        asyncClose()
    }
}