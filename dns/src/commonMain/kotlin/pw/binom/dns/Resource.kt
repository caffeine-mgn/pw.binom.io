package pw.binom.dns

import pw.binom.dns.protocol.ResourcePackage

data class Resource(
    val name: String,
    val type: Type,
    val clazz: Class,
    val ttl: UInt,
    val rdata: ByteArray,
) {

    val sizeBytes
        get() = name.length + 1 +
            Short.SIZE_BYTES +
            Short.SIZE_BYTES +
            Int.SIZE_BYTES +
            Short.SIZE_BYTES +
            rdata.size

    fun toMutable(resource: ResourcePackage): ResourcePackage {
        resource.name = name
        resource.type = type
        resource.clazz = clazz
        resource.ttl = ttl
        resource.rdata = rdata
        return resource
    }
}
