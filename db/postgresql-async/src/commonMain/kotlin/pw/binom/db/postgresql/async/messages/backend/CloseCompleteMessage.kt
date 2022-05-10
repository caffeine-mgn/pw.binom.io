package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

object CloseCompleteMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.CloseComplete

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.CloseComplete)
        writer.startBody()
        writer.endBody()
    }

    suspend fun read(ctx: PackageReader): CloseCompleteMessage {
        ctx.end()
        return CloseCompleteMessage
    }
}
