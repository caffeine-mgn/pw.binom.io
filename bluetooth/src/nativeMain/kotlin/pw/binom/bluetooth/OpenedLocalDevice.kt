package pw.binom.bluetooth

import kotlinx.cinterop.*
import platform.bluetooth.*
import pw.binom.io.Closeable
import pw.binom.io.ClosedException
import kotlin.concurrent.AtomicInt
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
actual class OpenedLocalDevice(val nativeDevice: CPointer<NOpennedDevice>) : Closeable {
  private val closed = AtomicInt(0)

  private fun ensureOpened() {
    if (closed.value != 0) {
      throw ClosedException()
    }
  }

  actual fun discover(): List<RemoteDevice> {
    ensureOpened()
    val remoteDevices = searchRemoteDevices(nativeDevice)
    val list = ArrayList<RemoteDevice>()
    try {
      var i = remoteDevices
      while (i != null) {
        val name = i.pointed.name.toKString()
        list += RemoteDevice(address = Address(i.pointed.address.readBytes(6)), name = name)
        i = i.pointed.next
      }
    } finally {
      freeRemoteDevices(remoteDevices)
    }
    return list
  }

  actual fun openSPP(remoteAddress: Address, channel: Int): SPPConnection {
    ensureOpened()
    val connection = remoteAddress.raw.usePinned { addressPinned ->
      connectSPP(
        device = nativeDevice,
        removeDeviceAddress = addressPinned.addressOf(0).reinterpret(),
        channel = channel,
      ) ?: TODO("Can't open connection to $remoteAddress")
    }
    return SPPConnection(connection)
  }

  actual override fun close() {
    if (!closed.compareAndSet(0, 1)) {
      return
    }
    closeLocalDevice(nativeDevice)
  }

  actual fun isDiscoverable(): Boolean {
    ensureOpened()
    val value = getLocalDeviceDiscoverable(nativeDevice)
    return when {
      value >= 1 -> true
      value == 0 -> false
      else -> TODO()
    }
  }

  actual fun setDiscoverable(value: Boolean) {
    ensureOpened()
    val result = setLocalDeviceDiscoverable(nativeDevice, if (value) 1 else 0)
    if (result <= 0) {
      TODO("--->111result=$result")
    }
    println("--->111result=$result")
  }

  actual fun publishSPP(channel: Int): SPPServer {
    val ptr = publishSPP(nativeDevice, channel) ?: TODO()
    return SPPServer(ptr)
  }

  actual fun openL2CAP(remoteAddress: Address, psm: PSM): SPPConnection {
    val connection = remoteAddress.raw.usePinned { addressPinned ->
      connectL2CAP(
        device = nativeDevice,
        removeDeviceAddress = addressPinned.addressOf(0).reinterpret(),
        psm = psm.value,
      )
    } ?: TODO("Can't open L2CAP connection to $remoteAddress")
    return SPPConnection(connection)
  }
}
