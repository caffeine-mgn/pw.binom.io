package pw.binom.db

import pw.binom.io.AsyncCloseable

interface AsyncResultSet : ResultSet, AsyncCloseable {
    suspend fun next(): Boolean
}

suspend inline fun AsyncResultSet.forEach(func: suspend (AsyncResultSet) -> Unit) {
    while (next()) {
        func(this)
    }
}

suspend inline fun AsyncResultSet.read(func: suspend (AsyncResultSet) -> Unit) {
    try {
        forEach(func)
    } finally {
        asyncClose()
    }
}