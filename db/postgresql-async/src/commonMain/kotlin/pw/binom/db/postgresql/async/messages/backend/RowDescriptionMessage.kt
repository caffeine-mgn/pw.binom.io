package pw.binom.db.postgresql.async.messages.backend

import pw.binom.db.postgresql.async.ColumnMeta
import pw.binom.db.postgresql.async.PackageReader
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class RowDescriptionMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.RowDescription

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "RowDescriptionMessage(columns=${columns.contentToString()})"
    }

    var columns: Array<ColumnMeta> = emptyArray()

    companion object {
        suspend fun read(ctx: PackageReader): RowDescriptionMessage {
            val columnsCount = ctx.readShort()
            val msg = ctx.rowDescriptionMessage
            msg.columns.forEach {
                ctx.recycleColumnData(it)
            }
            msg.columns = Array(columnsCount.toInt()) {
                val meta = ctx.giveColumnData()
                meta.name = ctx.readCString()
                meta.tableObjectId = ctx.readInt()
                meta.columnNumber = ctx.readShort().toInt()
                meta.dataType = ctx.readInt()
                meta.dataTypeSize = ctx.readShort().toLong()
                meta.dataTypeModifier = ctx.readInt()
                meta.fieldFormat = ctx.readShort().toInt()
                meta
            }
            ctx.end()
            return msg
        }
    }
}
