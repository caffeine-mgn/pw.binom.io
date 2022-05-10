package pw.binom.io

fun Channel.asAsyncChannel() = object : AsyncChannel {
    override suspend fun asyncClose() {
        this@asAsyncChannel.close()
    }

    override suspend fun write(data: ByteBuffer): Int =
        this@asAsyncChannel.write(data)

    override suspend fun flush() {
        this@asAsyncChannel.flush()
    }

    override val available: Int
        get() = -1

    override suspend fun read(dest: ByteBuffer): Int =
        this@asAsyncChannel.read(dest)
}
