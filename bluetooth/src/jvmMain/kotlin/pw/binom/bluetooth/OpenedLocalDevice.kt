package pw.binom.bluetooth

import com.sun.jna.Pointer
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import java.util.concurrent.atomic.AtomicBoolean

actual class OpenedLocalDevice(val native: Pointer) : Closeable {
  private val closed = AtomicBoolean(false)
  private fun ensureOpened() {
    if (!closed.get()) {
      throw ClosedException()
    }
  }

  actual fun discover(): List<RemoteDevice> {
    ensureOpened()
    val remoteDevices = NativeLibrary.INSTANCE.searchRemoteDevices(native)
    val list = ArrayList<RemoteDevice>()
    try {
      var i = remoteDevices
      while (i != null) {
        val el = NativeLibrary.NRemoteDevice.ByReference(i)
        val name = el.name.asString()
        list += RemoteDevice(address = Address(el.address), name = name)
        i = el.next
      }
    } finally {
      NativeLibrary.INSTANCE.freeRemoteDevices(remoteDevices)
    }
    return list
  }

  actual fun openSPP(removeAddress: Address, channel: Int): SPPConnection {
    ensureOpened()
    val connection = NativeLibrary.INSTANCE.openSPP(
      device = native,
      removeDeviceAddress = removeAddress.raw,
      channel = channel,
    ) ?: TODO("Can't open connection to $removeAddress")
    return SPPConnection(connection)
  }

  actual fun isDiscoverable(): Boolean {
    ensureOpened()
    val value = NativeLibrary.INSTANCE.getLocalDeviceDiscoverable(native)
    return when {
      value >= 1 -> true
      value == 0 -> false
      else -> TODO()
    }
  }

  actual fun setDiscoverable(value: Boolean) {
    ensureOpened()
    val result = NativeLibrary.INSTANCE.setLocalDeviceDiscoverable(native, if (value) 1 else 0)
    if (result <= 0) {
      TODO("--->111result=$result")
    }
    println("--->111result=$result")
  }

  override fun close() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    NativeLibrary.INSTANCE.closeLocalDevice(native)
  }
}
