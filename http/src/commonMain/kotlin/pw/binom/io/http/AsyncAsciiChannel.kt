package pw.binom.io.http

import pw.binom.ByteBufferPool
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.bufferedAsciiWriter

open class AsyncAsciiChannel(
    pool: ByteBufferPool,
    val channel: AsyncChannel,
) : AsyncCloseable {
    //    var reader = channel.bufferedAsciiReader(closeParent = false, bufferSize = 50)
//    var writer = channel.bufferedAsciiWriter(closeParent = false, bufferSize = 50)
    var reader = channel.bufferedAsciiReader(closeParent = false, pool = pool)
    var writer = channel.bufferedAsciiWriter(closeParent = false, pool = pool)
    override suspend fun asyncClose() {
        try {
            reader.asyncCloseAnyway()
            writer.asyncCloseAnyway()
        } finally {
            channel.asyncCloseAnyway()
        }
    }
}
