package pw.binom.dns

import pw.binom.dns.protocol.fromDns
import pw.binom.dns.protocol.toDnsString
import kotlin.jvm.JvmInline

@JvmInline
value class Domain(val raw: ByteArray) {
  companion object {
    fun create(domain: String) = Domain(toDns(domain))

    fun toDns(domain: String): ByteArray {
      val charArray = domain.toDnsString()
      return ByteArray(charArray.size) { charArray[it].code.toByte() }
    }

    fun fromDns(data: ByteArray): String =
      CharArray(data.size) {
        data[it].toInt().toChar()
      }.fromDns()
  }

  override fun toString() = fromDns(raw)
}
