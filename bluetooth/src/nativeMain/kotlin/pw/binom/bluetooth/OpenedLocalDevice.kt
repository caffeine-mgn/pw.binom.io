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

  actual fun discover(time: Int): List<RemoteDevice> {
    ensureOpened()
    val remoteDevices = searchRemoteDevices(nativeDevice, time)
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

  actual fun spdRequest(address: Address) {
    val r = address.raw.usePinned { addressPinned ->
      println("Calling spdRequest $address")
      val e = SDP_Request(nativeDevice, addressPinned.addressOf(0).reinterpret())
      println("Called spdRequest $address")
      e
    }
    println("-----------=====PTR: ${r?.toLong()}=====------------------")
//    return
    var record = r
    var counter = 0

    while (record != null) {
      counter++
      val name = record.pointed.name?.toKString()
      val uuid = record.pointed.uuid?.toKString()
      var service = record.pointed.services
      println("name: $name, uuid: $uuid, service: $service")
      while (service != null) {
        var attr = service.pointed.attributes
        while (attr != null) {
          val value = when (attr.pointed.type) {
            ATTRIBUTE_TYPE_UUID16.toUByte() -> attr.pointed.data?.reinterpret<ByteVarOf<Byte>>()?.toKString()
            else -> null
          }
          println("attrId=${attr.pointed.attrId} type=${attr.pointed.type} value=$value")
          attr = attr.pointed.next
        }
        service = service.pointed.next
      }
      record = record.pointed.next
    }
    println("counter: $counter")
    free_SDPService(r)
  }
}
