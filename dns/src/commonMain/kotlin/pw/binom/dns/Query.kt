package pw.binom.dns

import pw.binom.dns.protocol.Query

data class Query(
    var name: String,
    var type: UShort,
    var clazz: UShort,
) {
    fun toMutable(query: Query):Query {
        query.name = name
        query.type = type
        query.clazz = clazz
        return query
    }
}