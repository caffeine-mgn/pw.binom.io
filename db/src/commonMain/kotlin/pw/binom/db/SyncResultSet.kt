package pw.binom.db

import pw.binom.io.Closeable

interface SyncResultSet : ResultSet, Closeable{
    fun next(): Boolean

    fun <T> map(func: (ResultSet) -> T): Iterator<T> = object : Iterator<T> {
        private var end = this@SyncResultSet.next()
        override fun hasNext(): Boolean = end

        override fun next(): T {
            val r = func(this@SyncResultSet)
            end = this@SyncResultSet.next()
            return r
        }
    }
}