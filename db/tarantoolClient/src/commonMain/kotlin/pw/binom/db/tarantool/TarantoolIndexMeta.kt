package pw.binom.db.tarantool

const val VINDEX_IID_FIELD_NUMBER = 1
const val VINDEX_NAME_FIELD_NUMBER = 2
const val VINDEX_TYPE_FIELD_NUMBER = 3
const val VINDEX_OPTIONS_FIELD_NUMBER = 4
const val VINDEX_PARTS_FIELD_NUMBER = 5

const val VINDEX_PART_FIELD = 0
const val VINDEX_PART_TYPE = 1

data class TarantoolIndexMeta(
        val id: Int,
        val name: String,
        val type: String,
        val options: IndexOptions,
        val parts: List<IndexPart>,
) {

    data class IndexPart(
            val fieldNumber: Int,
            val type: String,
    )

    data class IndexOptions(val unique: Boolean)

    companion object {
        fun create(tuple: List<Any?>): TarantoolIndexMeta {
            val optionsMap = tuple[VINDEX_OPTIONS_FIELD_NUMBER] as Map<String, Any>

            var parts: List<IndexPart> = emptyList()
            val partsTuple = tuple[VINDEX_PARTS_FIELD_NUMBER] as List<*>
            if (!partsTuple.isEmpty()) {
                if (partsTuple[0] is List<*>) {
                    parts = (partsTuple as List<List<*>>)
                            .map { part ->
                                IndexPart(
                                        part[VINDEX_PART_FIELD] as Int,
                                        part[VINDEX_PART_TYPE] as String
                                )
                            }
                } else if (partsTuple[0] is Map<*, *>) {
                    parts = (partsTuple as List<Map<String?, Any?>>)
                            .map { part -> IndexPart(part["field"] as Int, part["type"] as String) }
                }
            }

            return TarantoolIndexMeta(
                    tuple[VINDEX_IID_FIELD_NUMBER] as Int,
                    tuple[VINDEX_NAME_FIELD_NUMBER] as String,
                    tuple[VINDEX_TYPE_FIELD_NUMBER] as String,
                    IndexOptions(optionsMap["unique"] as Boolean),
                    parts
            )
        }
    }
}