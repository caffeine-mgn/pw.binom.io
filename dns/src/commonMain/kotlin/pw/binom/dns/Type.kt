package pw.binom.dns

import kotlin.jvm.JvmInline

/**
 * Dns Record type
 * @see [Type.java](https://github.com/dnsjava/dnsjava/blob/master/src/main/java/org/xbill/DNS/Type.java)
 */
@JvmInline
value class Type(val raw: UShort) {
  companion object {
    /**
     * a host address
     */
    val A = Type(1u)

    /**
     * an authoritative name server
     */
    val NS = Type(2u)

    /**
     * a mail destination (Obsolete - use MX)
     *
     */
    val MD = Type(3u)

    /**
     * a mail forwarder (Obsolete - use MX)
     */
    val MF = Type(4u)

    /**
     * the canonical name for an alias
     */
    val CNAME = Type(5u)

    /**
     * marks the start of a zone of authority
     */
    val SOA = Type(6u)

    /**
     * a mailbox domain name (EXPERIMENTAL)
     */
    val MB = Type(7u)

    /**
     * a mail group member (EXPERIMENTAL)
     */
    val MG = Type(8u)

    /**
     * a mail rename domain name (EXPERIMENTAL)
     */
    val MR = Type(9u)

    /**
     * a null RR (EXPERIMENTAL)
     */
    val NULL = Type(10u)

    /**
     * a well known service description
     */
    val WKS = Type(11u)

    /**
     * a domain name pointer
     */
    val PTR = Type(12u)

    /**
     * host information
     */
    val HINFO = Type(13u)

    /**
     * mailbox or mail list information
     */
    val MINFO = Type(14u)

    /**
     * mail exchange
     */
    val MX = Type(15u)

    /**
     * text strings
     */
    val TXT = Type(16u)
    val AAAA = Type(28u)
    val OPT = Type(41u)
  }

  override fun toString(): String = when (raw) {
    A.raw -> "A"
    NS.raw -> "NS"
    MD.raw -> "MD"
    MF.raw -> "MF"
    CNAME.raw -> "CNAME"
    SOA.raw -> "SOA"
    MB.raw -> "MB"
    MG.raw -> "MG"
    MR.raw -> "MR"
    NULL.raw -> "NULL"
    WKS.raw -> "WKS"
    PTR.raw -> "PTR"
    HINFO.raw -> "HINFO"
    MINFO.raw -> "MINFO"
    MX.raw -> "MX"
    TXT.raw -> "TXT"
    AAAA.raw -> "AAAA"
    OPT.raw -> "OPT"
    else -> raw.toString()
  }
}
