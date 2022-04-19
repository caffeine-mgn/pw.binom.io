package pw.binom.io.http

import pw.binom.ByteBuffer
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.bufferedAsciiWriter
import pw.binom.pool.ObjectPool

open class AsyncAsciiChannel(
    pool: ObjectPool<ByteBuffer>,
    val channel: AsyncChannel,
) : AsyncCloseable {
    var reader = channel.bufferedAsciiReader(closeParent = false, pool = pool)
    var writer = channel.bufferedAsciiWriter(closeParent = false, pool = pool)
    override suspend fun asyncClose() {
        runCatching { reader.asyncClose() }
        runCatching { writer.asyncClose() }
        channel.asyncClose()
    }
}
