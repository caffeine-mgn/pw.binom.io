package pw.binom.db.postgresql.async.messages.backend

import pw.binom.collections.defaultHashMap
import pw.binom.db.postgresql.async.InformationParser
import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class NoticeMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Notice

    val fields = defaultHashMap<Char, String>()

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "NoticeMessage(fields=$fields)"

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
