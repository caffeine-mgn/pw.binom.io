package pw.binom.db.postgresql.async.messages.frontend

import pw.binom.UUID
import pw.binom.db.SQLException
import pw.binom.db.postgresql.async.ColumnTypes
import pw.binom.db.postgresql.async.PackageWriter
import pw.binom.db.postgresql.async.messages.KindedMessage
import pw.binom.db.postgresql.async.messages.MessageKinds
import pw.binom.writeUUID

class BindMessage : KindedMessage {
    override val kind: Byte
        get() = MessageKinds.Bind

    override fun write(writer: PackageWriter) {
        if (!valuesTypes.isEmpty()) {
            check(valuesTypes.size == values.size)
        }
        writer.writeCmd(MessageKinds.Bind)
        writer.startBody()
        writer.writeCString(statement)
        writer.writeCString(portal)

//        writer.writeShort((values?.size ?: 0).toShort())
//        writer.writeShort(if (binary) 1 else 0)
//        writer.writeShort(if (binary) 1 else 0)

        if (values.isEmpty() || valuesTypes.isEmpty()) {
            writer.writeShort(0)
        } else {
            writer.writeShort(valuesTypes.size.toShort())
            valuesTypes.forEach {
                writer.writeShort(1)
            }
        }

        writer.writeShort((values.size).toShort())
//        values?.forEach {
//            writer.writeShort(0)
//        }
        if (valuesTypes.isNotEmpty()) {
            values.forEachIndexed { index, it ->
                TypeWriter.writeBinary(valuesTypes[index], it, writer)
            }
        } else {
            values.forEach {
                TypeWriter.writeText(it, writer)
            }
        }
        if (binaryResult) {
            writer.writeShort(1)
            writer.writeShort(1)
        } else {
            writer.writeShort(0)
        }
//        writer.writeShort(resultColumnsTypes.size.toShort())
//        resultColumnsTypes.forEach {
//            writer.writeInt(it)
//        }

        writer.endBody()
    }

    var statement: String = ""
    var portal: String = ""
    var values: Array<Any?> = emptyArray()
    var valuesTypes: List<Int> = emptyList()
    var binaryResult = false
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
            ColumnTypes.UUID -> {
                when (value) {
                    is UUID -> {
                        writer.writeInt(16)
                        writer.output.writeUUID(writer.buf16, value)
                    }
                    else -> throwNotSupported("UUID")
                }
            }
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
            is Float, Double, Long, Int, UUID -> value.toString()
            is Boolean -> if (value) "t" else "f"
            is UUID -> value.toString()
            else -> throw SQLException("Unsopported type ${value::class}")
        }

        writer.writeLengthString(txt)
    }
}