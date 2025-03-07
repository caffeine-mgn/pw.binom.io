package pw.binom.bluetooth

import com.sun.jna.Pointer

actual object Devices {
  actual fun getDevices(): List<LocalDevice> {
    var ptr: Pointer? = NativeLibrary.INSTANCE.getLocalDevices()
    val list = ArrayList<LocalDevice>()
    while (ptr != Pointer.NULL && ptr != null) {
      val r = NativeLibrary.NLocalDevice.ByReference(ptr)
      val e = LocalDevice(ptr)
      ptr = r.next
      list += e
    }
    NativeLibrary.INSTANCE.detachLocalDevices(ptr)
    return list
  }
}
