package pw.binom.bluetooth

import pw.binom.io.Closeable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

expect class OpenedLocalDevice : Closeable {
  fun discover(time: Int): List<RemoteDevice>
  fun openSPP(remoteAddress: Address, channel: Int): SPPConnection
  fun openL2CAP(remoteAddress: Address, psm: PSM): SPPConnection
  fun isDiscoverable(): Boolean
  fun setDiscoverable(value: Boolean)
  fun publishSPP(channel: Int = -1): SPPServer
  fun spdRequest(address: Address)
  override fun close()
}

fun OpenedLocalDevice.discover(time: Duration) =
  discover((time.inWholeMilliseconds / 1.28.seconds.inWholeMilliseconds).toInt())
