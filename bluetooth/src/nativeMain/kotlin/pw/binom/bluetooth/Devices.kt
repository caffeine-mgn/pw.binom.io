package pw.binom.bluetooth

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed

@OptIn(ExperimentalForeignApi::class)
actual object Devices {
  actual fun getDevices(): List<LocalDevice> {
    var e = platform.bluetooth.getLocalDevices()
    val result = ArrayList<LocalDevice>()
    while (e != null) {
      result += LocalDevice(e)
      e = e.pointed.next
    }
    return result
  }
}
