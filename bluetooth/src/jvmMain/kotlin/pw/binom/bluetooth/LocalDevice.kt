package pw.binom.bluetooth

import com.sun.jna.*;
import java.lang.ref.Cleaner

actual class LocalDevice(val nativeDevice: Pointer) {

  actual val address: Address
  actual val name: String

  override fun toString(): String =
    "Device(address='$address', name='$name')"

  private val cleaner = NativeLibrary.createCleaner(nativeDevice) {
    NativeLibrary.INSTANCE.freeLocalDevices(it)
  }

  init {
    val device = NativeLibrary.NLocalDevice.ByReference(nativeDevice)
    address = Address(device.address)
    name = device.name.asString()
  }

  actual fun open(): OpenedLocalDevice =
    OpenedLocalDevice(NativeLibrary.INSTANCE.openLocalDevice(nativeDevice))
}
