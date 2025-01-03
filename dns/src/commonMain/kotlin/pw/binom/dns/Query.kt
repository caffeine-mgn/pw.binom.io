package pw.binom.dns

import pw.binom.dns.protocol.QueryPackage

data class Query(
  var name: String,
  var type: QType,
  var clazz: QClass,
) {

  private val dnsNameLengthInBytes
    get() = (if (name.isEmpty()) 0 else 1) + name.length + 1

  val sizeBytes
    get() = dnsNameLengthInBytes +
      Short.SIZE_BYTES +
      Short.SIZE_BYTES

  fun toMutable(): QueryPackage = toMutable(QueryPackage())

  fun toMutable(query: QueryPackage): QueryPackage {
    query.name = name
    query.type = type
    query.clazz = clazz
    return query
  }
}
