package pw.binom.dns

import pw.binom.dns.protocol.Resource

data class Resource(
    val name: String,
    val type: UShort,
    val clazz: UShort,
    val ttl: UInt,
    val rdata: ByteArray,
) {
    fun toMutable(resource: Resource): Resource {
        resource.name = name
        resource.type = type
        resource.clazz = clazz
        resource.ttl = ttl
        resource.rdata = rdata
        return resource
    }
}