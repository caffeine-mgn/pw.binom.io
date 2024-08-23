package pw.binom.io.socket

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress as JvmInetSocketAddress

actual open class InetSocketAddress(var native: JvmInetSocketAddress) : SocketAddress {
  actual companion object {
    actual fun resolveOrNull(host: String, port: Int): InetSocketAddress? =
      InetAddress.resolveOrNull(host)?.withPort(port)

    actual fun resolve(host: String, port: Int): InetSocketAddress =
      InetAddress.resolve(host).withPort(port)

    actual fun resolveAll(
      host: String,
      port: Int,
    ): List<InetSocketAddress> = InetAddress.resolveAll(host).map { it.withPort(port) }
  }

  actual val address: ByteArray
    get() = native.address.address
  actual val protocolFamily: ProtocolFamily
    get() = when (native.address) {
      is Inet4Address -> ProtocolFamily.AF_INET
      is Inet6Address -> ProtocolFamily.AF_INET6
      else -> ProtocolFamily.AF_INET
    }
  actual val isMulticastAddress: Boolean
    get() = native.address.isMulticastAddress
  actual override val host: String
    get() = native.address.hostAddress
  actual override val port: Int
    get() = native.port

  actual fun toMutable(): MutableInetSocketAddress {
    val l = MutableInetSocketAddress()
    toMutable(l)
    return l
  }

  actual fun toMutable(dest: MutableInetSocketAddress): MutableInetSocketAddress {
    dest.native = native
    return dest
  }

  override fun resolve(): InetSocketAddress = this
  override fun resolveAll(): List<InetSocketAddress> = listOf(this)
  override fun resolveOrNull(): InetSocketAddress? = this
  override fun toString(): String = "$host:$port"
}
