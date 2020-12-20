package pw.binom.db.postgresql.async

class ColumnMeta {
    var name: String = ""
    var tableObjectId: Int = 0
    var columnNumber: Int = 0
    var dataType: Int = 0
    var dataTypeSize: Long = 0
    var dataTypeModifier: Int = 0
    var fieldFormat: Int = 0
}