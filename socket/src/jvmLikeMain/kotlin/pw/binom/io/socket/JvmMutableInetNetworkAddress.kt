package pw.binom.io.socket
/*
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

class JvmMutableInetNetworkAddress : MutableInetNetworkAddress {
  var native: InetAddress? = null

  override val protocolFamily: ProtocolFamily
    get() = when (native) {
      is Inet4Address -> ProtocolFamily.AF_INET
      is Inet6Address -> ProtocolFamily.AF_INET6
      else -> ProtocolFamily.AF_INET
    }
  override val isMulticastAddress: Boolean
    get() = native?.isMulticastAddress ?: false
  override var host: String
    get() {
      return native?.hostAddress ?: ""
    }
    set(value) {
      native!!.isMulticastAddress
      native = InetAddress.getByName(value)
    }
}

internal actual fun createMutableInetNetworkAddress(): MutableInetNetworkAddress =
  JvmMutableInetNetworkAddress()

internal actual fun createInetNetworkAddress(host: String): InetNetworkAddress {
  InetAddress.getAllByName(host).toList()
  val addr = JvmMutableInetNetworkAddress()
  addr.host = host
  return addr
}
*/
