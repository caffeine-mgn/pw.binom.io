package pw.binom.dns.protocol

import pw.binom.*

class QueryPackage {
    var name: String = ""
    var type: UShort = 0u
    var clazz: UShort = 0u

    fun read(buf: ByteBuffer):QueryPackage {
        name = buf.readDns().fromDns()
        type = buf.readShort().toUShort()
        clazz = buf.readShort().toUShort()
        return this
    }

    fun write(buf: ByteBuffer) {
        buf.writeDns(name.toDnsString())
        buf.writeShort(type.toShort())
        buf.writeShort(clazz.toShort())
    }

    fun toImmutable() = pw.binom.dns.Query(
        name = name,
        type = type,
        clazz = clazz
    )

    override fun toString(): String {
        return "Query(name='$name', type=$type, clazz=$clazz)"
    }


}