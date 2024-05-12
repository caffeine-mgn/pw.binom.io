package pw.binom.upnp

open class DefaultUPnPPublication(device: UPnPDevice) : UPnPPublication {
  final override val message: ByteArray

  init {
    val sb = StringBuilder()
    sb.append("HTTP/1.1 200 OK\r\n")
    device.data.forEach {
      sb.append(it.key.uppercase()).append(":").append(it.value).append("\r\n")
    }
    message = sb.toString().encodeToByteArray()
  }
}
