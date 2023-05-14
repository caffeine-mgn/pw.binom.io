package pw.binom.dns.protocol

import pw.binom.dns.Class
import pw.binom.dns.Resource
import pw.binom.dns.Type
import pw.binom.io.ByteBuffer
import pw.binom.readInt
import pw.binom.readShort
import pw.binom.writeInt
import pw.binom.writeShort

data class ResourcePackage(
    var name: String = "",
    var type: Type = Type(0u),
    var clazz: Class = Class(0u),
    var ttl: UInt = 0u,
    var rdata: ByteArray = byteArrayOf(),
) {

    private val dnsNameLengthInBytes
        get() = (if (name.isEmpty()) 0 else 1) + name.length + 1

    fun read(buf: ByteBuffer): ResourcePackage {
        name = buf.readDns().fromDns()
        type = Type(buf.readShort().toUShort())
        clazz = Class(buf.readShort().toUShort())
        ttl = buf.readInt().toUInt()
        val dataSize = buf.readShort().toUShort().toInt() and 0xFF
        rdata = ByteArray(dataSize) {
            buf.getByte()
        }
        return this
    }

    val sizeBytes
        get() = dnsNameLengthInBytes +
            Short.SIZE_BYTES +
            Short.SIZE_BYTES +
            Int.SIZE_BYTES +
            Short.SIZE_BYTES +
            rdata.size

    fun write(buf: ByteBuffer) {
        buf.writeDns(name.toDnsString())
        buf.writeShort(type.raw.toShort())
        buf.writeShort(clazz.raw.toShort())
        buf.writeInt(ttl.toInt())
        buf.writeShort(rdata.size.toShort())
        buf.write(rdata)
    }

    fun toImmutable() =
        Resource(
            name = name,
            type = type,
            clazz = clazz,
            ttl = ttl,
            rdata = rdata,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ResourcePackage

        if (name != other.name) return false
        if (type != other.type) return false
        if (clazz != other.clazz) return false
        if (ttl != other.ttl) return false
        return rdata.contentEquals(other.rdata)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + clazz.hashCode()
        result = 31 * result + ttl.hashCode()
        result = 31 * result + rdata.contentHashCode()
        return result
    }
}
