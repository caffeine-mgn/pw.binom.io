package pw.binom.dns

import pw.binom.*

class Query {
    var name: String = ""
    var type: UShort = 0u
    var clazz: UShort = 0u

    fun read(buf: ByteBuffer) {
        name = buf.readDns().fromDns()
        type = buf.readShort().toUShort()
        clazz = buf.readShort().toUShort()
    }

    fun write(buf: ByteBuffer) {
        buf.writeDns(name.toDnsString())
        buf.writeShort(type.toShort())
        buf.writeShort(clazz.toShort())
    }

    override fun toString(): String {
        return "Query(name='$name', type=$type, clazz=$clazz)"
    }


}