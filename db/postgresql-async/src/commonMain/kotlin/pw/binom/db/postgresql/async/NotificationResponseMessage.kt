package pw.binom.db.postgresql.async

import pw.binom.readInt

class NotificationResponseMessage : KindedMessage {

    var backendPid: Int = 0
    var channel: String = ""
    var payload: String = ""

    override val kind: Byte
        get() = TODO("Not yet implemented")

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    companion object {
        suspend fun read(ctx: PackageReader): NotificationResponseMessage {
            val msg = ctx.notificationResponseMessage
            msg.backendPid = ctx.input.readInt(ctx.buf16)
            msg.channel = ctx.readCString()
            msg.payload = ctx.readCString()
            return msg
        }
    }
}