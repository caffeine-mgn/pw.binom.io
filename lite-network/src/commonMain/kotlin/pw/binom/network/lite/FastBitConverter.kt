package pw.binom.network.lite

import pw.binom.toByteArray
import kotlin.jvm.JvmName

object FastBitConverter {
    fun GetBytes(data: ByteArray, offset: Int, value: Short) {
        value.toByteArray(dest = data, offset = offset)
    }

    fun GetBytes(data: ByteArray, offset: Int, value: UShort) {
        GetBytes(
            data = data,
            offset = offset,
            value = value.toShort(),
        )
    }

    fun GetBytes(data: ByteArray, offset: Int, value: Long) {
        value.toByteArray(dest = data, offset = offset)
    }

    @JvmName("GetBytes2")
    fun GetBytes(data: ByteArray, offset: Int, value: ULong) {
        GetBytes(data = data, offset = offset, value = value.toLong())
    }

    @JvmName("GetBytes2")
    fun GetBytes(data: ByteArray, offset: Int, value: UInt) {
        GetBytes(data = data, offset = offset, value = value.toInt())
    }

    fun GetBytes(data: ByteArray, offset: Int, value: Int) {
        value.toByteArray(dest = data, offset = offset)
    }

    fun GetBytes(data: ByteArray, offset: Int, value: Double) {
        value.toByteArray(dest = data, offset = offset)
    }

    fun GetBytes(data: ByteArray, offset: Int, value: Float) {
        value.toByteArray(dest = data, offset = offset)
    }
}
