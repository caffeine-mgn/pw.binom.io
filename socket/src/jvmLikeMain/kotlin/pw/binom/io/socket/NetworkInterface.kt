package pw.binom.io.socket

import java.net.NetworkInterface as JNetworkInterface

private fun ip4ToString(data: ByteArray): String {
  val sb = StringBuilder(3 * 4 + 3)
  var first = true
  data.forEach {
    if (!first) {
      sb.append(".")
    }
    first = false
    sb.append(it.toUByte().toString())
  }
  return sb.toString()
}

private fun ip6ToString(data: ByteArray): String {
  val sb = StringBuilder(2 * 16 + 7)
  data.forEachIndexed { index, byte ->
    if (index % 2 == 0 && index > 1) {
      sb.append(":")
    }
    sb.append(byte.toUByte().toString(16).padStart(2, '0'))
  }
  return sb.toString()
}

private fun ipToString(data: ByteArray) =
  when (data.size) {
    4 -> ip4ToString(data)
    16 -> ip6ToString(data)
    else -> TODO()
  }

internal actual fun getAvailableNetworkInterfaces(): List<NetworkInterface> {
  val out = ArrayList<NetworkInterface>()
  JNetworkInterface
    .getNetworkInterfaces()
    .asIterator()
    .forEach { net ->
      net.interfaceAddresses.forEach { addr ->
        out += JvmNetworkInterface(
          ip = InetAddress(addr.address),
          name = net.name,
          prefixLength = addr.networkPrefixLength.toInt(),
          native = net,
          index = net.index,
        )
      }
    }
  return out
}

private data class JvmNetworkInterface(
  override val ip: InetAddress,
  override val name: String,
  override val prefixLength: Int,
  val native: java.net.NetworkInterface, override val index: Int,
) : NetworkInterface
