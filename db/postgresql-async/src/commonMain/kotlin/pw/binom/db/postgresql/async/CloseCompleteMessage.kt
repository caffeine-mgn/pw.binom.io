package pw.binom.db.postgresql.async

object CloseCompleteMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.CloseComplete

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    suspend fun read(ctx: PackageReader): CloseCompleteMessage {
        ctx.end()
        return CloseCompleteMessage
    }
}