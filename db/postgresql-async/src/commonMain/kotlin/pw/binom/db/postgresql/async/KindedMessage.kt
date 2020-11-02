package pw.binom.db.postgresql.async

import pw.binom.AsyncInput
import pw.binom.io.IOException
import pw.binom.readByte
import pw.binom.readInt

interface KindedMessage {
    val kind: Byte
    fun write(writer: PackageWriter)

    companion object {
        suspend fun read(ctx: PackageReader): KindedMessage {
            val cmd = ctx.rawInput.readByte(ctx.buf16)
            val len = ctx.rawInput.readInt(ctx.buf16) - Int.SIZE_BYTES
            require(len >= 0)
            ctx.startBody(len)
            println("cmd: #$cmd (${cmd.toChar()}), len: $len")
            return when (cmd) {
                MessageKinds.Authentication -> AuthenticationMessage.read(ctx)
                MessageKinds.Error -> ErrorMessage.read(ctx)
                MessageKinds.ParameterStatus -> ParameterStatusMessage.read(ctx)
                MessageKinds.BackendKeyData -> ProcessData.read(ctx)
                MessageKinds.ReadyForQuery -> ReadyForQueryMessage.read(ctx)
                MessageKinds.CommandComplete -> CommandCompleteMessage.read(ctx)
                MessageKinds.RowDescription -> RowDescriptionMessage.read(ctx)
                MessageKinds.DataRow -> DataRowMessage.read(ctx)
                MessageKinds.CloseComplete -> CloseCompleteMessage.read(ctx)
                MessageKinds.BindComplete -> BindCompleteMessage.read(ctx)
                MessageKinds.EmptyQueryString -> EmptyQueryStringMessage.read(ctx)
                MessageKinds.NoData -> NoDataMessage.read(ctx)
                MessageKinds.ParseComplete -> ParseCompleteMessage.read(ctx)
                MessageKinds.Notice -> NoticeMessage.read(ctx)
                MessageKinds.NotificationResponse -> NotificationResponseMessage.read(ctx)
                else -> throw IOException("Unknown CMD #$cmd (${cmd.toChar()})")
            }
        }
    }
}