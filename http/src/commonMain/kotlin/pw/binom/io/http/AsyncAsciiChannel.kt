package pw.binom.io.http

import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.bufferedAsciiWriter

open class AsyncAsciiChannel(val channel: AsyncChannel) : AsyncCloseable {
    var reader = channel.bufferedAsciiReader(closeParent = false)
    var writer = channel.bufferedAsciiWriter(closeParent = false)
    override suspend fun asyncClose() {
        runCatching { reader.asyncClose() }
        runCatching { writer.asyncClose() }
        channel.asyncClose()
    }
}