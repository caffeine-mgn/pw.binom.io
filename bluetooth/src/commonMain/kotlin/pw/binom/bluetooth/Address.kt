package pw.binom.bluetooth

import kotlin.jvm.JvmInline

@OptIn(ExperimentalStdlibApi::class)
@JvmInline
value class Address(val raw: ByteArray) {
  init {
    require(raw.size == 6) { "Invalid address ${raw.toHexString()}" }
  }

  companion object {
    fun parse(address: String): Address {
      val items = address.split(':')
      require(items.size == 6) { "Invalid address $address" }
      return Address(ByteArray(6) { items[it].toUByte(16).toByte() })
    }
  }

  override fun toString(): String {
    val sb = StringBuilder(6 * 2 + 5)
    for (i in 0..5) {
      if (i > 0) {
        sb.append(":")
      }
      sb.append(raw[i].toUByte().toString(16).uppercase().padStart(2, '0'))
    }
    sb.toString()
    return sb.toString()
  }
}
