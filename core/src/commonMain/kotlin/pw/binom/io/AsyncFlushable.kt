package pw.binom.io

interface AsyncFlushable {
    suspend fun flush()
}
