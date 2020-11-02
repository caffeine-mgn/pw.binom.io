package pw.binom.db.postgresql.async

import pw.binom.AsyncInput
import pw.binom.readByte

class NoticeMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Notice

    val fields = HashMap<Char, String>()

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "Error: $fields"

    companion object {
        suspend fun read(ctx: PackageReader): NoticeMessage {
            val msg = ctx.noticeMessage
            msg.fields.clear()
            InformationParser.readTo(ctx, msg.fields)
//            while (true) {
//                val kind = ctx.input.readByte(ctx.buf16)
//
//                if (kind == 0.toByte()) {
//                    break
//                }
//
//                msg.fields[kind.toChar()] = ctx.readCString()
//            }
            return msg
        }
    }

}