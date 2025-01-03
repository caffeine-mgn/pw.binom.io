package pw.binom.dns

import kotlin.jvm.JvmInline

/**
 * Dns Record type
 * @see [QType.java](https://github.com/dnsjava/dnsjava/blob/master/src/main/java/org/xbill/DNS/Type.java)
 */
@JvmInline
value class QType(val raw: UShort) {
  companion object {
    /**
     * a host address
     */
    val A = QType(1u)

    /**
     * an authoritative name server
     */
    val NS = QType(2u)

    /**
     * a mail destination (Obsolete - use MX)
     *
     */
    val MD = QType(3u)

    /**
     * a mail forwarder (Obsolete - use MX)
     */
    val MF = QType(4u)

    /**
     * the canonical name for an alias
     */
    val CNAME = QType(5u)

    /**
     * marks the start of a zone of authority
     */
    val SOA = QType(6u)

    /**
     * a mailbox domain name (EXPERIMENTAL)
     */
    val MB = QType(7u)

    /**
     * a mail group member (EXPERIMENTAL)
     */
    val MG = QType(8u)

    /**
     * a mail rename domain name (EXPERIMENTAL)
     */
    val MR = QType(9u)

    /**
     * a null RR (EXPERIMENTAL)
     */
    val NULL = QType(10u)

    /**
     * a well known service description
     */
    val WKS = QType(11u)

    /**
     * a domain name pointer
     */
    val PTR = QType(12u)

    /**
     * host information
     */
    val HINFO = QType(13u)

    /**
     * mailbox or mail list information
     */
    val MINFO = QType(14u)

    /**
     * mail exchange
     */
    val MX = QType(15u)

    /**
     * text strings
     */
    val TXT = QType(16u)
    val AFSDB = QType(18u)
    val AAAA = QType(28u)
    val APL = QType(42u)
    val OPT = QType(41u)
    val CAA = QType(257u)
    val CDNSKEY = QType(60u)
    val CDS = QType(59u)
    val CERT = QType(37u)
    val CSYNC = QType(62u)
    val DHCID = QType(49u)
    val DLV = QType(32769u)
    val DNAME = QType(39u)
    val DNSKEY = QType(48u)
    val DS = QType(43u)
    val URI = QType(256u)
    val TSIG = QType(250u)
  }

  override fun toString(): String = when (raw) {
    A.raw -> "A"
    AFSDB.raw -> "AFSDB"
    CDNSKEY.raw -> "CDNSKEY"
    DS.raw -> "DS"
    TSIG.raw -> "DS"
    URI.raw -> "URI"
    DNSKEY.raw -> "DNSKEY"
    CDS.raw -> "CDS"
    CERT.raw -> "CERT"
    CSYNC.raw -> "CSYNC"
    DNAME.raw -> "DNAME"
    DLV.raw -> "DLV"
    DHCID.raw -> "DHCID"
    APL.raw -> "APL"
    NS.raw -> "NS"
    MD.raw -> "MD"
    CAA.raw -> "CAA"
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
