package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

object SyncMessage: KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Sync

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.Sync)
        writer.startBody()
        writer.endBody()
    }

}