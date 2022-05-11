package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class PreparedStatementOpeningMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Parse

    var statementId: String = ""
    var query: String = ""
    var valuesTypes: List<Int>? = null

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.Parse)
        writer.startBody()

        writer.writeCString(statementId)
        writer.writeCString(query)

        writer.writeShort(valuesTypes?.size?.toShort() ?: 0)
        valuesTypes?.forEach {
            writer.writeInt(it)
        }
        writer.endBody()
    }
}
