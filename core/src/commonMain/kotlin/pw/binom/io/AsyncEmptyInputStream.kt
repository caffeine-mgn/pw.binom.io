package pw.binom.io

object AsyncEmptyInputStream : AsyncInputStream {
    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int = 0

    override suspend fun close() {
    }

}