package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds
import pw.binom.readInt

class ProcessData : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.BackendKeyData

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.BackendKeyData)
        writer.startBody()
        writer.writeInt(processId)
        writer.writeInt(secretKey)
        writer.endBody()
    }

    var processId: Int = 0
    var secretKey: Int = 0

    companion object {
        suspend fun read(ctx: PackageReader): ProcessData {
            ctx.processData.processId = ctx.input.readInt(ctx.buf16)
            ctx.processData.secretKey = ctx.input.readInt(ctx.buf16)
            return ctx.processData
        }
    }
}
