package pw.binom.io.http

import pw.binom.io.*
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
