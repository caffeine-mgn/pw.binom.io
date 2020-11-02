package pw.binom.db.postgresql.async

class CommandCompleteMessage : KindedMessage {

    var rowsAffected: Int = 0
    var statusMessage: String = ""

    override val kind: Byte
        get() = MessageKinds.CommandComplete

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    override fun toString(): String = statusMessage

    companion object {
        suspend fun read(ctx: PackageReader): CommandCompleteMessage {
            val result = ctx.readCString()
            ctx.commandCompleteMessage.rowsAffected = when (val indexOfRowCount = result.lastIndexOf(' ')) {
                -1 -> 0
                else -> result.substring(indexOfRowCount).trim().toIntOrNull() ?: 0
            }
            ctx.commandCompleteMessage.statusMessage = result
            return ctx.commandCompleteMessage
        }
    }

}