package pw.binom.db.postgresql.async

object NoDataMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.NoData

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    suspend fun read(ctx: PackageReader): NoDataMessage {
        ctx.end()
        return NoDataMessage
    }
}