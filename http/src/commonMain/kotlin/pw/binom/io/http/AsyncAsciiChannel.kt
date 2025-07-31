package pw.binom.io.http

import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.bufferedAsciiReader
import pw.binom.io.bufferedAsciiWriter
import pw.binom.io.bufferedOutput

open class AsyncAsciiChannel private constructor(
  pool: ByteBufferPool?,
  val channel: AsyncChannel,
  val bufferSize: Int,
) : AsyncCloseable {
  constructor(channel: AsyncChannel, pool: ByteBufferPool) : this(
    pool = pool,
    channel = channel,
    bufferSize = 0,
  )

  constructor(channel: AsyncChannel, bufferSize: Int = DEFAULT_BUFFER_SIZE) : this(
    pool = null,
    channel = channel,
    bufferSize = bufferSize,
  )

  var reader = if (pool == null) {
    channel.bufferedAsciiReader(closeParent = false, bufferSize = bufferSize)
  } else {
    channel.bufferedAsciiReader(closeParent = false, pool = pool)
  }
  var writer = if (pool == null) {
    channel.bufferedOutput(closeStream = false, bufferSize = bufferSize)
  } else {
    channel.bufferedOutput(closeStream = false, pool = pool)
  }


    override suspend fun asyncClose() {
    try {
      reader.asyncCloseAnyway()
      writer.asyncCloseAnyway()
    } finally {
      channel.asyncCloseAnyway()
    }
  }
}

