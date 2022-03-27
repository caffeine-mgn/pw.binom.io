package pw.binom.db.internal

import kotlinx.serialization.Serializable

class Table2 {
    val columns = ArrayList<ColumnInfo>()
    val records = ArrayList<RecordLine>()

    fun insert(values: Map<String, Any?>) {
        values.keys.forEach { valueKey ->
            if (!columns.any { it.name == valueKey }) {
                TODO("Column $valueKey not found")
            }
        }
        val valueForInsert = columns.map {
            val value = values[it.name] ?: it.defaultValue
            if (value == null && !it.allowNull) {
                TODO("Значение Null не допустимо для атрибута ${it.name}")
            }
            Record.StringRecord(value = value as String, columnId = it.id)
        }
        records += RecordLine(valueForInsert)
    }
}

sealed interface Record {
    val columnId: Int

    class StringRecord(val value: String, override val columnId: Int) : Record
}

class RecordLine(val records: List<Record>)

@Serializable
data class ColumnInfo(
    val name: String,
    val type: ColumnType,
    val id: Int,
    val allowNull: Boolean,
    val defaultValue: String?,
)

@Serializable
enum class ColumnType {
    STRING,
}
