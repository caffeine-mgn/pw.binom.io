package pw.binom.db.postgresql.async

object ParseCompleteMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.ParseComplete

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    suspend fun read(ctx: PackageReader): ParseCompleteMessage {
        ctx.end()
        return ParseCompleteMessage
    }
}