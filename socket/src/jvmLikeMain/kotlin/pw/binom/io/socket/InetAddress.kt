package pw.binom.io.socket

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress as JvmInetAddress
import java.net.InetSocketAddress as JvmInetSocketAddress
import java.net.UnknownHostException as JvmUnknownHostException

actual open class InetAddress(var native: JvmInetAddress) : NetworkAddress {
  actual companion object {
    actual fun resolveOrNull(host: String): InetAddress? =
      try {
        InetAddress(JvmInetAddress.getByName(host))
      } catch (e: JvmUnknownHostException) {
        null
      }

    actual fun resolveAll(host: String): List<InetAddress> =
      try {
        JvmInetAddress.getAllByName(host).map { InetAddress(it) }
      } catch (e: JvmUnknownHostException) {
        emptyList()
      }

    actual fun create(address: InetSocketAddress): InetAddress =
      InetAddress(address.native.address)

    actual fun resolve(host: String): InetAddress =
      try {
        InetAddress(JvmInetAddress.getByName(host))
      } catch (e: JvmUnknownHostException) {
        throw UnknownHostException(host)
      }
  }

  actual val protocolFamily: ProtocolFamily
    get() = when (native) {
      is Inet4Address -> ProtocolFamily.AF_INET
      is Inet6Address -> ProtocolFamily.AF_INET6
      else -> ProtocolFamily.AF_INET
    }
  actual val isMulticastAddress: Boolean
    get() = native.isMulticastAddress
  actual override val host: String
    get() = native.hostAddress
  actual val address: ByteArray
    get() = native.address

  override fun toString(): String = "InetNetworkAddress($host)"
  actual fun withPort(port: Int): InetSocketAddress {
    return InetSocketAddress(JvmInetSocketAddress(native, port))
  }

  actual override fun resolve(): InetAddress? = this
  actual override fun resolveAll(): List<InetAddress> = listOf(this)
  actual fun toMutable(): MutableInetAddress {
    val result = MutableInetAddress()
    result.native = native
    return result
  }
}
