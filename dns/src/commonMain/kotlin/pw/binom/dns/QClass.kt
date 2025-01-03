package pw.binom.dns

import kotlin.jvm.JvmInline

@JvmInline
value class QClass(val raw: UShort) {
  companion object {
    val IN = QClass(1u)
    val CS = QClass(2u)
    val CH = QClass(3u)
    val HS = QClass(4u)
    val NONE = QClass(254u)
    val ANY = QClass(255u)
    val EDNS = QClass(1232u)
  }

  override fun toString(): String =
    when (raw) {
      IN.raw -> "IN"
      CS.raw -> "CS"
      CH.raw -> "CH"
      HS.raw -> "HS"
      EDNS.raw -> "EDNS"
      NONE.raw -> "NONE"
      ANY.raw -> "ANY"
      else -> raw.toString()
    }
}
