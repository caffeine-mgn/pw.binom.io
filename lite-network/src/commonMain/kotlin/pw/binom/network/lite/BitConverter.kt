package pw.binom.network.lite

import pw.binom.fromBytes

object BitConverter {
    fun ToInt16(data: ByteArray, offset: Int) = Short.fromBytes(data[0 + offset], data[1 + offset])
    fun ToUInt16(data: ByteArray, offset: Int) = ToInt16(data = data, offset = offset).toUShort()
    fun ToInt32(data: ByteArray, offset: Int) = Int.fromBytes(data, offset = offset)
    fun ToUInt32(data: ByteArray, offset: Int) = ToInt32(data = data, offset = offset)
    fun ToSingle(data: ByteArray, offset: Int) = Float.fromBits(ToInt32(data = data, offset = offset))

    fun ToInt64(data: ByteArray, offset: Int) = Long.fromBytes(data, offset = offset)
    fun ToUInt64(data: ByteArray, offset: Int) = ToInt64(data = data, offset = offset).toULong()
    fun ToDouble(data: ByteArray, offset: Int) = Double.fromBits(ToInt64(data = data, offset = offset))
}
