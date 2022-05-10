package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds
import pw.binom.readInt
import pw.binom.readShort

class DataRowMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.DataRow

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    var data: Array<ByteArray?> = emptyArray()

    companion object {
        suspend fun read(ctx: PackageReader): DataRowMessage {
            val columnCount = ctx.input.readShort(ctx.buf16)
            ctx.dataRowMessage.data = Array(columnCount.toInt()) {
                val length = ctx.input.readInt(ctx.buf16)
                when {
                    length < 0 -> null
                    length == 0 -> byteArrayOf()
                    else -> ctx.readByteArray(length)
                }
            }
            return ctx.dataRowMessage
        }
    }
}
