package pw.binom.db.postgresql.async

import pw.binom.readByte

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
            ctx.readyForQueryMessage.transactionStatus = ctx.input.readByte(ctx.buf16).toChar()
            return ctx.readyForQueryMessage
        }
    }

}