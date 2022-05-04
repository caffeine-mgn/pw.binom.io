package pw.binom.db.serialization

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
}
