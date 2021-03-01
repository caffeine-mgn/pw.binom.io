package pw.binom.io

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.ByteBuffer

interface AsyncChannel : AsyncCloseable, AsyncOutput, AsyncInput

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