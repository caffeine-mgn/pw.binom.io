package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class ExecuteMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Execute

    override fun write(writer: PackageWriter) {
        require(limit >= 0)
        writer.writeCmd(MessageKinds.Execute)
        writer.startBody()
        writer.writeCString(statementId)
        writer.writeInt(limit)
        writer.endBody()
    }

    var statementId = ""
    var limit = 0
}