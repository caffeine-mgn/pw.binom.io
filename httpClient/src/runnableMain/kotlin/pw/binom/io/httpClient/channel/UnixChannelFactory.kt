package pw.binom.io.httpClient.channel

import pw.binom.io.AsyncChannel
import pw.binom.io.file.File

interface UnixChannelFactory<T : AsyncChannel> : ChannelFactory<T> {
  suspend fun connect(path: String): T
  suspend fun connect(path: File): T = connect(path.path)
}
