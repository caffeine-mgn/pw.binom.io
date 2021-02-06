package pw.binom.db

import pw.binom.io.Closeable

interface SyncResultSet : ResultSet, Closeable {
    fun next(): Boolean

    fun <T> map(mapper: (ResultSet) -> T)=
        SyncResultSetIterator(this, mapper)
}