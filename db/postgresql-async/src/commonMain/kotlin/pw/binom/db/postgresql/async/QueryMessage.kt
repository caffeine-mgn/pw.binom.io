package pw.binom.db.postgresql.async

import pw.binom.*
import pw.binom.charset.Charset
import pw.binom.io.BufferedOutputAppendable
import pw.binom.io.ByteArrayOutput

class QueryMessage : KindedMessage {
    var query = ""

    override val kind
        get() = 'Q'.toByte()

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.Query)
        writer.startBody()
        writer.writeCString(query)
        writer.rewriteSize()
    }

    override fun toString(): String = "Query [$query]"
}