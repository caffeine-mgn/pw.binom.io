package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class DescribeMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Describe

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.Describe)
        writer.startBody()
        writer.writeByte(if (portal) 'P'.code.toByte() else 'S'.code.toByte())
        writer.writeCString(statement)
        writer.endBody()
    }

    var statement: String = ""
    var portal = true
}
