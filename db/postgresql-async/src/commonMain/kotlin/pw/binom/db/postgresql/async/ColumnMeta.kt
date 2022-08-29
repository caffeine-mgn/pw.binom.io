package pw.binom.db.postgresql.async

class ColumnMeta {
    var name: String = ""
    var tableObjectId: Int = 0
    var columnNumber: Int = 0
    var dataType: Int = 0
    var dataTypeSize: Long = 0
    var dataTypeModifier: Int = 0
    var fieldFormat: Int = 0
    override fun toString(): String {
        return "ColumnMeta(name='$name', tableObjectId=$tableObjectId, columnNumber=$columnNumber, dataType=$dataType, dataTypeSize=$dataTypeSize, dataTypeModifier=$dataTypeModifier, fieldFormat=$fieldFormat)"
    }
}
