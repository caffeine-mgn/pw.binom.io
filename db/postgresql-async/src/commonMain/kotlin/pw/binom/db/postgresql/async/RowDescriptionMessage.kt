package pw.binom.db.postgresql.async

import pw.binom.readInt
import pw.binom.readShort

class RowDescriptionMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.RowDescription

    override fun write(writer: PackageWriter) {
        TODO("Not yet implemented")
    }

    var columns: Array<ColumnMeta> = emptyArray()

    companion object {
        suspend fun read(ctx: PackageReader): RowDescriptionMessage {
            val columnsCount = ctx.input.readShort(ctx.buf16)
            val msg = ctx.rowDescriptionMessage
            msg.columns = Array(columnsCount.toInt()) {
                val meta = ctx.giveColumnData()
                meta.name = ctx.readCString()
                meta.tableObjectId = ctx.input.readInt(ctx.buf16)
                meta.columnNumber = ctx.input.readShort(ctx.buf16).toInt()
                meta.dataType = ctx.input.readInt(ctx.buf16)
                meta.dataTypeSize = ctx.input.readShort(ctx.buf16).toLong()
                meta.dataTypeModifier = ctx.input.readInt(ctx.buf16)
                meta.fieldFormat = ctx.input.readShort(ctx.buf16).toInt()
                meta
            }
            ctx.end()
            return msg
        }
    }
}