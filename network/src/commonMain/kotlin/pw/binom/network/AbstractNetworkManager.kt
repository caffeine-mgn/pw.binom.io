package pw.binom.network

import kotlinx.coroutines.CoroutineDispatcher
import pw.binom.io.socket.*

abstract class AbstractNetworkManager : CoroutineDispatcher(), NetworkManager {
  protected abstract val selector: Selector
  protected abstract fun ensureOpen()

  override fun attach(channel: TcpClientSocket, mode: ListenFlags): TcpConnection {
    ensureOpen()
    channel.blocking = false
    val key = selector.attach(socket = channel)
    if (!key.updateListenFlags(mode)) {
      if (key.isClosed) {
        throw IllegalStateException("SelectorKey is closed")
      }
      throw IllegalStateException("Can't update flags")
    }
    val con = TcpConnection(channel = channel, currentKey = key)
    key.attachment = con
    if (mode.isNotZero) {
      selector.wakeup()
    }
    return con
  }

  override fun attach(channel: TcpNetServerSocket): TcpServerConnection {
    ensureOpen()
    channel.blocking = false
    while (true) {
      val key = selector.attach(socket = channel)
      val con = TcpServerConnection(channel = channel, dispatcher = this, currentKey = key)
      if (!key.updateListenFlags(ListenFlags.ZERO)) {
        key.closeAnyway()
        continue
      }
      key.attachment = con
      return con
    }
  }

  override fun attach(channel: UdpNetSocket): UdpConnection {
    ensureOpen()
    val con = UdpConnection(channel)
    channel.blocking = false
    val key = selector.attach(socket = channel)
    if (!key.updateListenFlags(ListenFlags.ZERO)) {
      throw IllegalStateException("Can't listening flags")
    }
    key.attachment = con
    con.keys.addKey(key)
    return con
  }

  override fun attach(channel: MulticastUdpSocket): MulticastUdpConnection {
    ensureOpen()
    val con = MulticastUdpConnection(channel)
    channel.blocking = false
    val key = selector.attach(socket = channel)
    if (!key.updateListenFlags(ListenFlags.ZERO)) {
      throw IllegalStateException("Can't listening flags")
    }
    key.attachment = con
    con.keys.addKey(key)
    return con
  }

  protected fun internalWakeup() {
    selector.wakeup()
  }

  override fun wakeup() {
    ensureOpen()
    internalWakeup()
  }
}
