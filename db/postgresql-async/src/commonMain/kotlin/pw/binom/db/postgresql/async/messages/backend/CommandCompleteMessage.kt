package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class CommandCompleteMessage : KindedMessage {

    var rowsAffected: Int = 0
    var statusMessage: String = ""

    override val kind: Byte
        get() = MessageKinds.CommandComplete

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.CommandComplete)
        writer.startBody()
        writer.writeCString("$statusMessage $rowsAffected")
        writer.endBody()
    }

    override fun toString(): String = "CommandCompleteMessage(statusMessage: [$statusMessage], rowsAffected: [$rowsAffected])"

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