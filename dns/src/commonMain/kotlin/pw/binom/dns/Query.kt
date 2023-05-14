package pw.binom.dns

import pw.binom.dns.protocol.QueryPackage

data class Query(
    var name: String,
    var type: Type,
    var clazz: Class,
) {

    val sizeBytes
        get() = name.length + 1 +
            Short.SIZE_BYTES +
            Short.SIZE_BYTES

    fun toMutable(query: QueryPackage): QueryPackage {
        query.name = name
        query.type = type
        query.clazz = clazz
        return query
    }
}
