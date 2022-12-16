package pw.binom.db.serialization

import pw.binom.db.ColumnType

class ListStaticSyncResultSet(
    override val list: List<List<Any?>>,
    override val columns: List<String>
) : AbstractStaticSyncResultSet<List<Any?>>() {

    override fun getString(index: Int, value: List<Any?>): String? =
        value[index].toString()

    override fun getBlob(index: Int, value: List<Any?>): ByteArray? =
        when (val el = value[index]) {
            null -> null
            is ByteArray -> el
            is Collection<*> -> {
                val iter = el.iterator()
                ByteArray(el.size) {
                    iter.toString().toByte()
                }
            }

            else -> TODO("Can't cast ${el::class} to ByteArray")
        }

    override fun isNull(index: Int, value: List<Any?>): Boolean =
        value[index] == null

    override fun columnType(index: Int): ColumnType {
        TODO("Not yet implemented")
    }

    override fun columnType(column: String): ColumnType {
        TODO("Not yet implemented")
    }
}
