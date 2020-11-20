package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class CloseMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.CloseStatementOrPortal

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.CloseStatementOrPortal)
        writer.startBody()
        writer.writeByte(if (portal) 'P'.toByte() else 'S'.toByte())
        writer.writeCString(statement)
        writer.endBody()
    }

    override fun toString(): String =
        "CloseMessage(portal: [$portal], statement: [$statement])"

    var portal = true
    var statement: String = ""
}