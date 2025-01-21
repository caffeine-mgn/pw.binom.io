package pw.binom.io.httpClient.channel

import pw.binom.io.AsyncChannel
import pw.binom.io.socket.DomainSocketAddress

interface InetChannelFactory<T : AsyncChannel> : ChannelFactory<T> {
  suspend fun connect(address: DomainSocketAddress): T
}
