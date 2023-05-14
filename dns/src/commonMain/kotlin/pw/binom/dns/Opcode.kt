package pw.binom.dns

import kotlin.jvm.JvmInline

@JvmInline
value class Opcode(val raw: Byte) {
    companion object {
        /**
         * A standard query
         */
        val QUERY = Opcode(0)

        /**
         * An inverse query (deprecated)
         */
        val IQUERY = Opcode(1)

        /**
         * A server status request (not used)
         */
        val STATUS = Opcode(2)

        /**
         * A message from a primary to a secondary server to initiate a zone transfer
         */
        val NOTIFY = Opcode(4)

        /**
         * A dynamic update message
         */
        val UPDATE = Opcode(5)

        /**
         * DNS Stateful Operations (DSO, RFC8490)
         */
        val DSO = Opcode(5)
    }

    override fun toString(): String = when (raw) {
        QUERY.raw -> "QUERY"
        IQUERY.raw -> "IQUERY"
        STATUS.raw -> "STATUS"
        NOTIFY.raw -> "NOTIFY"
        UPDATE.raw -> "UPDATE"
        DSO.raw -> "DSO"
        else -> raw.toString()
    }
}
