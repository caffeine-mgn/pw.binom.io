package pw.binom.db.postgresql.async

class ParameterStatusMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.ParameterStatus

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    var key:String=""
    var value:String=""

    override fun toString(): String ="$key=$value"

    companion object{
        suspend fun read(ctx: PackageReader):ParameterStatusMessage{
            val msg = ctx.parameterStatusMessage
            msg.key=ctx.readCString()
            msg.value=ctx.readCString()
            return msg
        }
    }
}