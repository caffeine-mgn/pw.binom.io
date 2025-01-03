package pw.binom.dns.protocol

import pw.binom.dns.QClass
import pw.binom.dns.QType
import pw.binom.dns.QueryI
import pw.binom.io.ByteBuffer
import pw.binom.readShort
import pw.binom.writeShort

data class QueryPackage(
  var name: String = "",
  var type: QType = QType(0u),
  var clazz: QClass = QClass(0u),
) : QueryI {

  private val dnsNameLengthInBytes
    get() = (if (name.isEmpty()) 0 else 1) + name.length + 1

  val sizeBytes
    get() = dnsNameLengthInBytes +
      Short.SIZE_BYTES +
      Short.SIZE_BYTES

  fun read(buf: ByteBuffer): QueryPackage {
    name = buf.readDns().fromDns()
    type = QType(buf.readShort().toUShort())
    clazz = QClass(buf.readShort().toUShort())
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
