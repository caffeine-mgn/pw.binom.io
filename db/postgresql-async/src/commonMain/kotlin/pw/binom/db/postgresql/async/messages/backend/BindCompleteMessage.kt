package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

object BindCompleteMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.BindComplete

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.BindComplete)
        writer.startBody()
        writer.endBody()
    }

    suspend fun read(ctx: PackageReader): BindCompleteMessage {
        ctx.end()
        return BindCompleteMessage
    }

    override fun toString(): String = "BindCompleteMessage()"
}
