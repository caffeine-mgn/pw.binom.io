package pw.binom.db

import pw.binom.io.Closeable

class SyncResultSetIterator<T>(val resultSet: SyncResultSet, val mapper: (ResultSet) -> T) : Iterator<T>, Closeable {
    private var end = !resultSet.next()
    override fun hasNext(): Boolean = !end

    init {
        if (end) {
            resultSet.close()
        }
    }

    override fun next(): T {
        if (end) {
            throw NoSuchElementException()
        }
        val r = mapper(resultSet)
        end = !resultSet.next()
        if (end) {
            resultSet.close()
        }
        return r
    }

    override fun close() {
        try {
            if (!end) {
                resultSet.close()
            }
        } finally {
            end = true
        }
    }
}