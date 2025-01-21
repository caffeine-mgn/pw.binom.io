package pw.binom.io.httpClient.channel

import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable

interface ChannelFactory<T : AsyncChannel> : AsyncCloseable {
  suspend fun free(channel: T)
}
