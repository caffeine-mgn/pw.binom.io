package pw.binom.db

import pw.binom.io.AsyncCloseable

interface AsyncResultSet : ResultSet, AsyncCloseable{
    suspend fun next(): Boolean
}