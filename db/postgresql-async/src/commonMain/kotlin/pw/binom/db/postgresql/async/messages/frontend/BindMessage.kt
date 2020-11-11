package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.UUID
import pw.binom.db.postgresql.async.ColumnTypes
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds

class BindMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Bind

    override fun write(writer: PackageWriter) {
        if (!valuesTypes.isEmpty()) {
            check(valuesTypes.size == values!!.size)
        }
        writer.writeCmd(MessageKinds.Bind)
        writer.startBody()
        writer.writeCString(statement)
        writer.writeCString(portal)

//        writer.writeShort((values?.size ?: 0).toShort())
//        writer.writeShort(if (binary) 1 else 0)
//        writer.writeShort(if (binary) 1 else 0)

        if (valuesTypes.isEmpty()) {
            writer.writeShort(0)
        } else {
            writer.writeShort(valuesTypes.size.toShort())
            valuesTypes.forEach {
                writer.writeShort(1)
            }
        }

        writer.writeShort((values?.size ?: 0).toShort())
//        values?.forEach {
//            writer.writeShort(0)
//        }
        if (valuesTypes.isNotEmpty()) {
            values?.forEachIndexed { index, it ->
                TypeWriter.writeBinary(valuesTypes[index], it, writer)
            }
        } else {
            values?.forEach {
                TypeWriter.writeText(it, writer)
            }
        }
        writer.writeShort(0)

        writer.endBody()
    }

    var statement: String = ""
    var portal: String = ""
    var values: Array<Any?>? = null
    var valuesTypes: List<Int> = emptyList()
    var binary = false
}

object TypeWriter {
    fun writeBinary(type: Int, value: Any?, writer: PackageWriter) {

        if (value == null) {
            writer.writeInt(-1)
            return
        }

        fun throwNotSupported(type: String): Nothing =
            throw IllegalArgumentException("Not supported type. Can't cast ${value::class} to $type")

        when (type) {
            ColumnTypes.Integer -> when (value) {
                is Int -> {
                    writer.writeInt(4)
                    writer.writeInt(value)
                }
                is Long -> {
                    if (value > Int.MAX_VALUE || value < Int.MIN_VALUE) {
                        throw IllegalArgumentException("Can't cast Long to Int. Out of Range. value: [$value]")
                    }
                    writer.writeInt(4)
                    writer.writeInt(value.toInt())
                }
                is String -> {
                    val intValue = value.toIntOrNull()
                        ?: throw IllegalArgumentException("Can't convert ${value} to Integer. Invalid format")
                    writer.writeInt(4)
                    writer.writeInt(intValue)
                }
                else -> throwNotSupported("Integer")
            }
            ColumnTypes.Text -> {
                when (value) {
                    is Int, Float, Double, Long, Short, Byte, UUID -> {
                        writer.writeLengthString(value.toString())
                    }
                    is String -> {
                        writer.writeLengthString(value)
                    }
                    else -> throwNotSupported("String")
                }
            }
            else -> throw IllegalArgumentException("Not supported type. Type: [$type]")
        }
    }

    fun writeText(value: Any?, writer: PackageWriter) {
        if (value == null) {
            writer.writeInt(-1)
            return
        }
        val txt = when (value) {
            is String -> value
            is Int -> value.toString()
            else -> TODO()
        }

        writer.writeLengthString(txt)
    }
}