package pw.binom.io.httpClient.channel

import pw.binom.network.NetworkManager
import pw.binom.network.TcpConnection
import pw.binom.network.tcpConnectUnixSocket

class NetworkUnixChannelFactory(
  private val networkManager: NetworkManager,
) : UnixChannelFactory<TcpConnection> {
  override suspend fun free(channel: TcpConnection) {
    channel.closeAnyway()
  }

  override suspend fun connect(path: String): TcpConnection =
    networkManager.tcpConnectUnixSocket(path)


  override suspend fun asyncClose() {
    // Do nothing
  }
}
