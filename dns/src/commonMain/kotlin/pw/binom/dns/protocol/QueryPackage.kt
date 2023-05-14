package pw.binom.dns.protocol

import pw.binom.dns.Class
import pw.binom.dns.Type
import pw.binom.io.ByteBuffer
import pw.binom.readShort
import pw.binom.writeShort

data class QueryPackage(
    var name: String = "",
    var type: Type = Type(0u),
    var clazz: Class = Class(0u),
) {

    private val dnsNameLengthInBytes
        get() = (if (name.isEmpty()) 0 else 1) + name.length + 1

    val sizeBytes
        get() = dnsNameLengthInBytes +
            Short.SIZE_BYTES +
            Short.SIZE_BYTES

    fun read(buf: ByteBuffer): QueryPackage {
        name = buf.readDns().fromDns()
        type = Type(buf.readShort().toUShort())
        clazz = Class(buf.readShort().toUShort())
        return this
    }

    fun write(buf: ByteBuffer) {
        buf.writeDns(name.toDnsString())
        buf.writeShort(type.raw.toShort())
        buf.writeShort(clazz.raw.toShort())
    }

    fun toImmutable() = pw.binom.dns.Query(
        name = name,
        type = type,
        clazz = clazz,
    )
}
