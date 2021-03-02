package pw.binom.db

import pw.binom.io.Closeable

interface SyncResultSet : ResultSet, Closeable {
    fun next(): Boolean

    fun <T> map(mapper: (ResultSet) -> T) =
        SyncResultSetIterator(this, mapper)
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