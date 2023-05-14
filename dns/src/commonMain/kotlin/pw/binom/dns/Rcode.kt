package pw.binom.dns

import kotlin.jvm.JvmInline

@JvmInline
value class Rcode(val raw: Byte) {
    companion object {
        /**
         * No error
         */
        val NOERROR = Rcode(0)

        /**
         * Format error
         */
        val FORMERR = Rcode(1)

        /**
         * Server failure
         */
        val SERVFAIL = Rcode(2)

        /**
         * The name does not exist
         */
        val NXDOMAIN = Rcode(3)

        /**
         * The operation requested is not implemented
         */
        val NOTIMP = Rcode(4)

        /**
         * The operation was refused by the server
         */
        val REFUSED = Rcode(5)

        /**
         * The name exists
         */
        val YXDOMAIN = Rcode(6)

        /**
         * The RRset (name, type) exists
         */
        val YXRRSET = Rcode(7)

        /**
         * The RRset (name, type) does not exist
         */
        val NXRRSET = Rcode(8)

        /**
         * The requestor is not authorized to perform this operation
         */
        val NOTAUTH = Rcode(9)

        /**
         * The zone specified is not a zone
         */
        val NOTZONE = Rcode(10)

        // ----====EDNS extended rcodes====----

        /**
         * Unsupported EDNS level
         */
        val BADVERS = Rcode(16)

        // ----====SIG/TKEY only rcodes====----
        /**
         * The signature is invalid (TSIG/TKEY extended error)
         */
        val BADSIG = Rcode(16)

        /**
         * The key is invalid (TSIG/TKEY extended error)
         */
        val BADKEY = Rcode(17)

        /**
         * The time is out of range (TSIG/TKEY extended error)
         */
        val BADTIME = Rcode(18)

        /**
         * The mode is invalid (TKEY extended error)
         */
        val BADMODE = Rcode(19)

        /**
         * Duplicate key name (TKEY extended error)
         */
        val BADNAME = Rcode(20)

        /**
         * Algorithm not supported (TKEY extended error)
         */
        val BADALG = Rcode(21)

        /**
         * Bad truncation (RFC 4635)
         */
        val BADTRUNC = Rcode(22)

        /**
         * Bad or missing server cookie (RFC 7873)
         */
        val BADCOOKIE = Rcode(22)
    }

    override fun toString(): String = when (raw) {
        NOERROR.raw -> "NOERROR"
        FORMERR.raw -> "FORMERR"
        SERVFAIL.raw -> "SERVFAIL"
        NXDOMAIN.raw -> "NXDOMAIN"
        NOTIMP.raw -> "NOTIMP"
        REFUSED.raw -> "REFUSED"
        YXDOMAIN.raw -> "YXDOMAIN"
        YXRRSET.raw -> "YXRRSET"
        NXRRSET.raw -> "NXRRSET"
        NOTAUTH.raw -> "NOTAUTH"
        NOTZONE.raw -> "NOTZONE"
        BADVERS.raw -> "BADVERS"
        BADKEY.raw -> "BADKEY"
        BADTIME.raw -> "BADTIME"
        BADMODE.raw -> "BADMODE"
        BADNAME.raw -> "BADNAME"
        BADALG.raw -> "BADALG"
        BADTRUNC.raw -> "BADTRUNC"
        BADCOOKIE.raw -> "BADCOOKIE"
        else -> raw.toString()
    }
}
