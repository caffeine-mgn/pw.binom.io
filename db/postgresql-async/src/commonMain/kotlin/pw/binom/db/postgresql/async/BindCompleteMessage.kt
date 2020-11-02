package pw.binom.db.postgresql.async

object BindCompleteMessage:KindedMessage{
    override val kind: Byte
        get() = MessageKinds.BindComplete

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    suspend fun read(ctx: PackageReader): BindCompleteMessage {
        ctx.end()
        return BindCompleteMessage
    }

}