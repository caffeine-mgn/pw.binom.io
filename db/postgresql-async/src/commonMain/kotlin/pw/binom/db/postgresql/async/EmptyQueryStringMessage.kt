package pw.binom.db.postgresql.async

object EmptyQueryStringMessage:KindedMessage{
    override val kind: Byte
        get() = MessageKinds.EmptyQueryString

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    suspend fun read(ctx: PackageReader): EmptyQueryStringMessage {
        ctx.end()
        return EmptyQueryStringMessage
    }
}