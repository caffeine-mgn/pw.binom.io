package pw.binom.io.socket

import pw.binom.dump
import pw.binom.toByteArray
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

private fun ipv4Mask(prefixLength: Int): String {
  val m = (UInt.MAX_VALUE) shl (32 - prefixLength)
  return ip4ToString(m.toInt().toByteArray())
}

private fun ipv6Mask(prefixLength: Int): String {
  val ip = ByteArray(16)
  return when {
    prefixLength == Long.SIZE_BITS -> {
      repeat(Long.SIZE_BYTES) {
        ip[it] = UByte.MAX_VALUE.toByte()
      }
      ip6ToString(ip)
    }

    prefixLength == Long.SIZE_BITS * 2 -> {
      repeat(Long.SIZE_BYTES * 2) {
        ip[it] = UByte.MAX_VALUE.toByte()
      }
      ip6ToString(ip)
    }

    prefixLength < Long.SIZE_BITS -> {
      (ULong.MAX_VALUE shl (Long.SIZE_BITS - prefixLength)).toLong().dump(ip)
      ip6ToString(ip)
    }

    prefixLength < Long.SIZE_BITS * 2 -> {
      repeat(Long.SIZE_BYTES) {
        ip[it] = UByte.MAX_VALUE.toByte()
      }
      (ULong.MAX_VALUE shl (Long.SIZE_BITS - prefixLength)).toLong().dump(ip, destOffset = Long.SIZE_BYTES)
      ip6ToString(ip)
    }

    else -> TODO("prefixLength=$prefixLength")
  }
}

internal actual fun getAvailableNetworkInterfaces(): List<NetworkInterface> {
  val out = ArrayList<NetworkInterface>()
  JNetworkInterface
    .getNetworkInterfaces()
    .asIterator()
    .forEach { net ->
      net.interfaceAddresses.forEach { addr ->
        out += JvmNetworkInterface(
          ip = ipToString(addr.address.address),
          name = net.name,
          prefixLength = addr.networkPrefixLength.toInt(),
        )
      }
    }
  return out
}

private data class JvmNetworkInterface(
  override val ip: String,
  override val name: String,
  override val prefixLength: Int,
) : NetworkInterface
