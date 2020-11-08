package pw.binom.db.postgresql.async

import pw.binom.UUID

class PreparedStatementOpeningMessage(
    val statementId: String,
    val query: String,
    val valuesTypes: List<Int>,
) : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Parse

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.Parse)
        writer.startBody()

        writer.writeCString(statementId)
        writer.writeCString(query)
        writer.rewriteSize()

        writer.writeShort(valuesTypes.size.toShort())
        writer.rewriteSize()
    }

}