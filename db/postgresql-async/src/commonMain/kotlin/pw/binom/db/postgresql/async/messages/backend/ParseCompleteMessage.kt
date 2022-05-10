package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

object ParseCompleteMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.ParseComplete

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.ParseComplete)
        writer.startBody()
        writer.endBody()
    }

    suspend fun read(ctx: PackageReader): ParseCompleteMessage {
        ctx.end()
        return ParseCompleteMessage
    }
}
