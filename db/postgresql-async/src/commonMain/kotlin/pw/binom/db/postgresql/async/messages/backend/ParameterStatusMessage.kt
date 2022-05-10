package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class ParameterStatusMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.ParameterStatus

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.ParameterStatus)
        writer.startBody()
        writer.writeCString(key)
        writer.writeCString(value)
        writer.endBody()
    }

    var key: String = ""
    var value: String = ""

    override fun toString(): String = "ParameterStatusMessage($key=$value)"

    companion object {
        suspend fun read(ctx: PackageReader): ParameterStatusMessage {
            val msg = ctx.parameterStatusMessage
            msg.key = ctx.readCString()
            msg.value = ctx.readCString()
            return msg
        }
    }
}
