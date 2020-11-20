package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

object EmptyQueryStringMessage: KindedMessage {
    override val kind: Byte
        get() = MessageKinds.EmptyQueryString

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.EmptyQueryString)
        writer.startBody()
        writer.endBody()
    }

    suspend fun read(ctx: PackageReader): EmptyQueryStringMessage {
        ctx.end()
        return EmptyQueryStringMessage
    }
}