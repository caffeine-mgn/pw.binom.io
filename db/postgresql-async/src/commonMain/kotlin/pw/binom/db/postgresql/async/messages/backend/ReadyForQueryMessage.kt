package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class ReadyForQueryMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.ReadyForQuery

    var transactionStatus: Char = '0'

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "ReadyForQueryMessage($transactionStatus)"

    companion object {
        suspend fun read(ctx: PackageReader): ReadyForQueryMessage {
            ctx.readyForQueryMessage.transactionStatus = ctx.readByte().toInt().toChar()
            ctx.end()
            return ctx.readyForQueryMessage
        }
    }
}
