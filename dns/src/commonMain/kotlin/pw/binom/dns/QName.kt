package pw.binom.dns

import pw.binom.dns.protocol.fromDns
import pw.binom.dns.protocol.toDnsString
import kotlin.jvm.JvmInline

@JvmInline
value class QName private constructor(private val raw: ByteArray) {
  companion object {
    fun create(domain: String) = QName(toDns(domain))
    fun create(data: ByteArray) = QName(data)

    fun toDns(domain: String): ByteArray {
      val charArray = domain.toDnsString()
      return ByteArray(charArray.size) { charArray[it].code.toByte() }
    }

    fun fromDns(data: ByteArray): String =
      CharArray(data.size) {
        data[it].toInt().toChar()
      }.fromDns()
  }

  val asByteArray
    get() = raw
  val asString
    get() = fromDns(raw)

  override fun toString() = asString
}
