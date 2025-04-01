package pw.binom.bluetooth

import pw.binom.io.Closeable

expect class OpenedLocalDevice : Closeable {
  fun discover(): List<RemoteDevice>
  fun openSPP(remoteAddress: Address, channel: Int): SPPConnection
  fun openL2CAP(remoteAddress: Address, psm: PSM): SPPConnection
  fun isDiscoverable(): Boolean
  fun setDiscoverable(value: Boolean)
  fun publishSPP(channel: Int = -1): SPPServer
  override fun close()
}
