package pw.binom.dns.protocol

import pw.binom.dns.Resource
import pw.binom.io.ByteBuffer
import pw.binom.readInt
import pw.binom.readShort
import pw.binom.writeInt
import pw.binom.writeShort

class ResourcePackage {
    var name: String = ""
    var type: UShort = 0u
    var clazz: UShort = 0u
    var ttl: UInt = 0u
    var rdata: ByteArray = byteArrayOf()

    fun read(buf: ByteBuffer): ResourcePackage {
        name = buf.readDns().fromDns()
        type = buf.readShort().toUShort()
        clazz = buf.readShort().toUShort()
        ttl = buf.readInt().toUInt()
        val dataSize = buf.readShort().toUShort().toInt() and 0xFF
        rdata = ByteArray(dataSize) {
            buf.getByte()
        }
        return this
    }

    val sizeBytes
        get() = name.length + 1 +
            Short.SIZE_BYTES +
            Short.SIZE_BYTES +
            Int.SIZE_BYTES +
            Short.SIZE_BYTES +
            rdata.size

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
