package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class Terminate: KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Terminate

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.Query)
        writer.startBody()
        writer.endBody()
    }
}