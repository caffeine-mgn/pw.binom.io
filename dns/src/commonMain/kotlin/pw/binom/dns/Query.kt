package pw.binom.dns

import pw.binom.dns.protocol.QueryPackage

data class Query(
    var name: String,
    var type: UShort,
    var clazz: UShort,
) {
    fun toMutable(query: QueryPackage):QueryPackage {
        query.name = name
        query.type = type
        query.clazz = clazz
        return query
    }
}