package pw.binom.db.radis

class RemoteList(val connection: RadisConnection, val key: String) {
    suspend fun getSize() = connection.getListSize(key) ?: 0L
    suspend fun addLast(value: String) {
        connection.insertLast(key = key, value = value)
    }

    suspend fun addFirst(value: String) {
        connection.insertFirst(key = key, value = value)
    }

    suspend fun toList() = connection.getList(key, start = 0, end = -1)
}
