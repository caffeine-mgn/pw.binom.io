package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.InformationParser
import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class ErrorMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Error

    val fields = HashMap<Char, String>()

    override fun write(writer: PackageWriter) {
        writer.writeCmd(MessageKinds.Error)
        writer.startBody()
        fields.forEach {
            writer.writeByte(it.key.toByte())
            writer.writeCString(it.value)
        }
        writer.writeByte(0)
        writer.endBody()
    }

    override fun toString(): String = "Error: $fields"

    companion object {
        suspend fun read(ctx: PackageReader): ErrorMessage {
            val msg = ctx.errorMessage
            msg.fields.clear()
            InformationParser.readTo(ctx, msg.fields)
            return msg
        }
    }

}