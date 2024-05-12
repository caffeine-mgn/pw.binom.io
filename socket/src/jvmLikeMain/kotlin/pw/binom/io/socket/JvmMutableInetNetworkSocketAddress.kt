package pw.binom.io.socket
/*

import java.net.InetAddress
import java.net.InetSocketAddress

class JvmMutableInetNetworkSocketAddress() : MutableInetNetworkSocketAddress {

  constructor(address: InetNetworkSocketAddress) : this() {
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

  override fun clone(): MutableInetNetworkSocketAddress {
    val ret = JvmMutableInetNetworkSocketAddress()
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

  override fun toMutable(dest: MutableInetNetworkSocketAddress): MutableInetNetworkSocketAddress {
    if (dest is JvmMutableInetNetworkSocketAddress) {
      dest.native = native
    } else {
      dest.update(
        host = host,
        port = port,
      )
    }
    return dest
  }

  override fun toMutable(): MutableInetNetworkSocketAddress = this
  override fun toImmutable(): InetNetworkSocketAddress = JvmMutableInetNetworkSocketAddress(this)
}
*/
