package pw.binom.dns.protocol

import pw.binom.*
import pw.binom.dns.Resource

class ResourcePackage {
    var name: String = ""
    var type: UShort = 0u
    var clazz: UShort = 0u
    var ttl: UInt = 0u
    var rdata: ByteArray = byteArrayOf()

    fun read(buf: ByteBuffer): pw.binom.dns.protocol.ResourcePackage {
        name = buf.readDns().fromDns()
        type = buf.readShort().toUShort()
        clazz = buf.readShort().toUShort()
        ttl = buf.readInt().toUInt()
        val dataSize = buf.readShort().toUShort().toInt() and 0xFF
        rdata = ByteArray(dataSize) {
            buf.get()
        }
        return this
    }

    fun write(buf: ByteBuffer) {
        buf.writeDns(name.toDnsString())
        buf.writeShort(type.toShort())
        buf.writeShort(clazz.toShort())
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

    override fun toString(): String {
        return "Resource(name='$name', type=$type, clazz=$clazz, ttl=$ttl, rdata=${
            rdata.map {
                it.toUByte().toString(16)
            }
        })"
    }


}