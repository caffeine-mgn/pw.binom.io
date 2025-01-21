package pw.binom.io.httpClient.channel

import pw.binom.io.socket.DomainSocketAddress
import pw.binom.network.NetworkManager
import pw.binom.network.TcpConnection
import pw.binom.network.tcpConnect

class NetworkInetChannelFactory(
  private val networkManager: NetworkManager,
) : InetChannelFactory<TcpConnection> {
  override suspend fun free(channel: TcpConnection) {
    channel.closeAnyway()
  }

  override suspend fun connect(address: DomainSocketAddress): TcpConnection =
    networkManager.tcpConnect(address.resolve())

  override suspend fun asyncClose() {
    // Do nothing
  }
}
