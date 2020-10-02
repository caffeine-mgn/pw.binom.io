package pw.binom.db.tarantool

internal const val VSPACE_ID_FIELD_NUMBER = 0
internal const val VSPACE_FORMAT_FIELD_NUMBER = 6
internal const val VSPACE_NAME_FIELD_NUMBER = 2;
internal const val VSPACE_ENGINE_FIELD_NUMBER = 3;

data class TarantoolSpaceMeta(
        val id: Int, val name: String,
        val engine: String,
        val format: List<SpaceField>,
        val indexes: Map<String, TarantoolIndexMeta>,
) {
    companion object {
        fun create(spaceTuple: List<Any?>, indexTuples: List<List<Any?>>): TarantoolSpaceMeta {
            val id = (spaceTuple[VSPACE_ID_FIELD_NUMBER] as Number).toInt()
            val fields = (spaceTuple[VSPACE_FORMAT_FIELD_NUMBER] as (List<Map<String, Any>>)).map {
                SpaceField(
                        name = it["name"] as String,
                        type = it["type"] as String,
                        nullable = it["is_nullable"] == true
                )
            }

            val indexesMap = indexTuples
                    .asSequence()
                    .map { TarantoolIndexMeta.create(it) }
                    .map { it.name to it }
                    .toMap()

            return TarantoolSpaceMeta(
                    id = id,
                    name = spaceTuple[VSPACE_NAME_FIELD_NUMBER] as String,
                    engine = spaceTuple[VSPACE_ENGINE_FIELD_NUMBER] as String,
                    format = fields,
                    indexes = indexesMap,
            )
        }
    }
}