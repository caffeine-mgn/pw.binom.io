package pw.binom.bluetooth

import pw.binom.io.Closeable

expect class OpenedLocalDevice : Closeable {
  fun discover(): List<RemoteDevice>
  fun openSPP(removeAddress: Address, channel: Int): SPPConnection
  fun isDiscoverable():Boolean
  fun setDiscoverable(value: Boolean)
}
