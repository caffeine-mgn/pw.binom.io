package pw.binom.http.client.factory

import pw.binom.io.AsyncChannel
import pw.binom.io.socket.DomainSocketAddress
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect

class NativeNetChannelFactory(val manager: NetworkManager) : NetSocketFactory {
  override suspend fun connect(host: String, port: Int): AsyncChannel {
    val address = DomainSocketAddress(
      host = host,
      port = port,
    ).resolve()
    val channel = manager.tcpConnect(address)
    return channel
  }
}
