package pw.binom.dns

import kotlin.jvm.JvmInline

/**
 * Dns Record type
 * @see [Type.java](https://github.com/dnsjava/dnsjava/blob/master/src/main/java/org/xbill/DNS/Type.java)
 */
@JvmInline
value class Type(val raw: UShort) {
    companion object {
        val A = Type(1u)
        val NS = Type(2u)
        val CNAME = Type(5u)
        val NULL = Type(10u)
        val TXT = Type(16u)
        val AAAA = Type(28u)
        val OPT = Type(41u)
    }

    override fun toString(): String = when (raw) {
        A.raw -> "A"
        NS.raw -> "NS"
        CNAME.raw -> "CNAME"
        NULL.raw -> "NULL"
        TXT.raw -> "TXT"
        AAAA.raw -> "AAAA"
        OPT.raw -> "OPT"
        else -> raw.toString()
    }
}
