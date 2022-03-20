package pw.binom.db.radis

import pw.binom.io.AsyncCloseable

interface RESP : AsyncCloseable {
    suspend fun flush()
}
