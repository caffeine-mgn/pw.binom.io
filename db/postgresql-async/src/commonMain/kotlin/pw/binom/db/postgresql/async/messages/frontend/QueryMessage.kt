package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class QueryMessage : KindedMessage {
    var query = ""

    override val kind
        get() = 'Q'.toByte()

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.Query)
        writer.startBody()
        writer.writeCString(query)
        writer.endBody()
    }

    override fun toString(): String = "Query [$query]"
}