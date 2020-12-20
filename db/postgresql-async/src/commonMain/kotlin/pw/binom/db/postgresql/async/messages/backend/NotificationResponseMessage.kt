package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds
import pw.binom.readInt

class NotificationResponseMessage : KindedMessage {

    var backendPid: Int = 0
    var channel: String = ""
    var payload: String = ""

    override val kind: Byte
        get() = MessageKinds.NotificationResponse

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.NotificationResponse)
        writer.startBody()
        writer.writeInt(backendPid)
        writer.writeCString(channel)
        writer.writeCString(payload)
        writer.endBody()
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