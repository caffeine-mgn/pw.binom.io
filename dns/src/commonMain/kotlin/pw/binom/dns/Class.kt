package pw.binom.dns

import kotlin.jvm.JvmInline

@JvmInline
value class Class(val raw: UShort) {
    companion object {
        val IN = Class(1u)
        val CS = Class(2u)
        val CH = Class(3u)
        val HS = Class(4u)
        val NONE = Class(254u)
        val ANY = Class(255u)
    }

    override fun toString(): String =
        when (raw) {
            IN.raw -> "IN"
            CS.raw -> "CS"
            CH.raw -> "CH"
            HS.raw -> "HS"
            NONE.raw -> "NONE"
            ANY.raw -> "ANY"
            else -> raw.toString()
        }
}
