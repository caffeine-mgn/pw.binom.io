package pw.binom.io.socket

import java.net.InetAddress
import java.net.InetSocketAddress

class JvmMutableInetNetworkAddress() : MutableInetNetworkAddress {

  constructor(address: InetNetworkAddress) : this() {
    update(
      host = address.host,
      port = address.port,
    )
  }

  var native: InetSocketAddress? = null

  override fun update(host: String, port: Int) {
    try {
      native = InetSocketAddress(InetAddress.getByName(host), port)
    } catch (e: java.net.UnknownHostException) {
      throw UnknownHostException(host)
    }
  }

  override fun toString(): String = "$host:$port"

  override fun clone(): MutableInetNetworkAddress {
    val ret = JvmMutableInetNetworkAddress()
    ret.update(
      host = host,
      port = port,
    )
    return ret
  }

  override val host: String
    get() {
      val native = native
      require(native != null)
      return native.address.hostAddress
    }
  override val port: Int
    get() {
      val native = native
      require(native != null)
      return native.port
    }

  override fun toMutable(dest: MutableInetNetworkAddress): MutableInetNetworkAddress {
    if (dest is JvmMutableInetNetworkAddress) {
      dest.native = native
    } else {
      dest.update(
        host = host,
        port = port,
      )
    }
    return dest
  }

  override fun toMutable(): MutableInetNetworkAddress = this
  override fun toImmutable(): InetNetworkAddress = JvmMutableInetNetworkAddress(this)
}
