package pw.binom.io

interface AsyncOutputStream:Closeable{
    suspend fun write(data: ByteArray, offset: Int, length: Int): Int
}