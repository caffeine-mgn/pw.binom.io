package pw.binom.db.postgresql.async

import pw.binom.db.ColumnType

class ColumnMeta {
    var name: String = ""
    var tableObjectId: Int = 0
    var columnNumber: Int = 0
    var dataType: Int = 0
    var dataTypeSize: Long = 0
    var dataTypeModifier: Int = 0
    var fieldFormat: Int = 0

    val columnType
        get() = when (dataType) {
            ColumnTypes.Text -> ColumnType.VARCHAR
            ColumnTypes.Integer -> ColumnType.INTEGER
            ColumnTypes.Boolean -> ColumnType.BIT
            ColumnTypes.Bigserial -> ColumnType.BIGINT
            ColumnTypes.Real -> ColumnType.FLOAT
            ColumnTypes.Double -> ColumnType.DOUBLE
            ColumnTypes.UUID -> ColumnType.UUID
            ColumnTypes.Smallint -> ColumnType.TINYINT
            ColumnTypes.Numeric -> ColumnType.NUMERIC
            ColumnTypes.Char -> ColumnType.CHAR
            ColumnTypes.Date -> ColumnType.DATE
            ColumnTypes.Time -> ColumnType.TIME
            ColumnTypes.Timestamp -> ColumnType.TIMESTAMP
            ColumnTypes.ByteA -> ColumnType.BINARY
            ColumnTypes.Varchar -> ColumnType.VARCHAR
            else -> error("Unknown data type $dataType")
        }

    override fun toString() =
        "ColumnMeta(name='$name', tableObjectId=$tableObjectId, columnNumber=$columnNumber, dataType=$dataType, dataTypeSize=$dataTypeSize, dataTypeModifier=$dataTypeModifier, fieldFormat=$fieldFormat)"
}
