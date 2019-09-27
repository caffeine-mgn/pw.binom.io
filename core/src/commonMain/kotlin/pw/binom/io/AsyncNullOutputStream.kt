package pw.binom.io

class AsyncNullOutputStream : AsyncOutputStream {
    override suspend fun write(data: Byte): Boolean = false

    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int = 0

    override suspend fun flush() {
    }

    override suspend fun close() {
    }

}