package pw.binom.bluetooth

import kotlinx.cinterop.*
import platform.posix.free
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner
import platform.bluetooth.NLocalDevice as NDevice

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
actual class LocalDevice(val nativeDevice: CPointer<NDevice>) {
  actual val address: Address = Address(nativeDevice.pointed.address.readBytes(6))
  actual val name: String = nativeDevice.pointed.name.toKString()
  private val cleaner = createCleaner(nativeDevice) {
    free(it)
  }

  override fun toString(): String =
    "Device(address='$address', name='$name')"


  actual fun open(): OpenedLocalDevice {
    val openedDevice = platform.bluetooth.openLocalDevice(nativeDevice) ?: TODO("Can't open device")
    return OpenedLocalDevice(openedDevice)
  }


}
