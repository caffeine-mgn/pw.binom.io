package pw.binom.db.postgresql.async

import pw.binom.db.ColumnType
import pw.binom.db.ResultSet
import pw.binom.uuid.UUID

/**
 * [PostgreSQL Type Formats](https://www.npgsql.org/dev/types.html)
 * [Codes](https://hackage.haskell.org/package/postgresql-simple-0.0.2/docs/src/Database-PostgreSQL-Simple-BuiltinTypes.html)
 */
object ColumnTypes {
    const val Untyped = 0
    const val Bigserial = 20
    const val BigserialArray = 1016
    const val Char = 18
    const val CharArray = 1002
    const val Smallint = 21
    const val SmallintArray = 1005
    const val Integer = 23
    const val IntegerArray = 1007
    const val Numeric = 1700

    // Decimal is the same as Numeric on PostgreSQL
    const val NumericArray = 1231
    const val Real = 700
    const val RealArray = 1021
    const val Double = 701
    const val DoubleArray = 1022
    const val Serial = 23
    const val Bpchar = 1042
    const val BpcharArray = 1014
    const val Varchar = 1043

    // Char is the same as Varchar on PostgreSQL
    const val VarcharArray = 1015
    const val Text = 25
    const val TextArray = 1009
    const val Timestamp = 1114
    const val TimestampArray = 1115
    const val TimestampWithTimezone = 1184
    const val TimestampWithTimezoneArray = 1185
    const val Date = 1082
    const val DateArray = 1182
    const val Time = 1083
    const val TimeArray = 1183
    const val TimeWithTimezone = 1266
    const val TimeWithTimezoneArray = 1270
    const val Interval = 1186
    const val IntervalArray = 1187
    const val Boolean = 16
    const val BooleanArray = 1000
    const val OID = 26
    const val OIDArray = 1028

    const val ByteA = 17
    const val ByteA_Array = 1001

    const val MoneyArray = 791
    const val NameArray = 1003
    const val UUID = 2950
    const val UUIDArray = 2951
    const val XMLArray = 143

    const val Inet = 869
    const val InetArray = 1041

    fun getValueType(value: Any): Int? =
        when (value) {
            is UUID -> UUID
            else -> null
        }
}

/**
 * Converts [ResultSet.ColumnType] to Postgresql Self Column Type
 */
internal val ColumnType.typeInt
    get() = when (this) {
        ColumnType.VARCHAR -> ColumnTypes.Text
        ColumnType.INTEGER -> ColumnTypes.Integer
        ColumnType.BIT -> ColumnTypes.Boolean
        ColumnType.BIGINT -> ColumnTypes.Bigserial
        ColumnType.FLOAT -> ColumnTypes.Real
        ColumnType.DOUBLE -> ColumnTypes.Double
        ColumnType.UUID -> ColumnTypes.UUID
        ColumnType.TINYINT -> ColumnTypes.Smallint
        ColumnType.SMALLINT -> ColumnTypes.Smallint
        ColumnType.REAL -> ColumnTypes.Real
        ColumnType.NUMERIC -> ColumnTypes.Numeric
        ColumnType.DECIMAL -> ColumnTypes.Double
        ColumnType.CHAR -> ColumnTypes.Char
        ColumnType.LONGVARCHAR -> TODO()
        ColumnType.DATE -> ColumnTypes.Date
        ColumnType.TIME -> ColumnTypes.Time
        ColumnType.TIMESTAMP -> ColumnTypes.Timestamp
        ColumnType.BINARY -> ColumnTypes.Date
        ColumnType.VARBINARY -> TODO()
        ColumnType.LONGVARBINARY -> TODO()
        ColumnType.NULL -> TODO()
        ColumnType.OTHER -> TODO()
    }
