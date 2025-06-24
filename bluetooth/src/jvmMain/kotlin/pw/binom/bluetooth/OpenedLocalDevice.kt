package pw.binom.bluetooth

import com.sun.jna.Pointer
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import java.util.concurrent.atomic.AtomicBoolean

actual class OpenedLocalDevice(val native: Pointer) : Closeable {
  private val closed = AtomicBoolean(false)
  private fun ensureOpened() {
    if (closed.get()) {
      throw ClosedException()
    }
  }

  actual fun discover(time: Int): List<RemoteDevice> {
    ensureOpened()
    val remoteDevices = NativeLibrary.INSTANCE.searchRemoteDevices(native, time)
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

  actual fun openSPP(remoteAddress: Address, channel: Int): SPPConnection {
    ensureOpened()
    val connection = NativeLibrary.INSTANCE.connectSPP(
      device = native,
      removeDeviceAddress = remoteAddress.raw,
      channel = channel,
    ) ?: TODO("Can't open connection to $remoteAddress")
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

  actual override fun close() {
    if (!closed.compareAndSet(false, true)) {
      return
    }
    NativeLibrary.INSTANCE.closeLocalDevice(native)
  }

  actual fun publishSPP(channel: Int): SPPServer {
    val ptr = NativeLibrary.INSTANCE.publishSPP(native, channel) ?: TODO()
    return SPPServer(ptr)
  }

  actual fun openL2CAP(
    remoteAddress: Address,
    psm: PSM,
  ): SPPConnection {
    ensureOpened()
    val connection = NativeLibrary.INSTANCE.connectL2CAP(
      device = native,
      removeDeviceAddress = remoteAddress.raw,
      psm = psm.value,
    ) ?: TODO("Can't open connection to $remoteAddress")
    return SPPConnection(connection)
  }

  actual fun spdRequest(address: Address) {
    NativeLibrary.INSTANCE.SDP_Request(native, address.raw)
  }
}
