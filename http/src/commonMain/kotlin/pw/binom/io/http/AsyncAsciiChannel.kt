package pw.binom.io.http

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.bufferedAsciiWriter

open class AsyncAsciiChannel(val channel: AsyncChannel, bufferSize: Int = DEFAULT_BUFFER_SIZE) : AsyncCloseable {
    var reader = channel.bufferedAsciiReader(closeParent = false, bufferSize = bufferSize)
    var writer = channel.bufferedAsciiWriter(closeParent = false, bufferSize = bufferSize)
    override suspend fun asyncClose() {
        runCatching { reader.asyncClose() }
        runCatching { writer.asyncClose() }
        channel.asyncClose()
    }
}