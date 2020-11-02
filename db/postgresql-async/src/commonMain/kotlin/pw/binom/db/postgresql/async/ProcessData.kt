package pw.binom.db.postgresql.async

import pw.binom.readInt

class ProcessData : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.BackendKeyData

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
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