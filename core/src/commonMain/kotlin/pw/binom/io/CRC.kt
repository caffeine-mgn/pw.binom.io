package pw.binom.io

import pw.binom.ByteDataBuffer

/**
 * https://stackoverflow.com/questions/27939882/fast-crc-algorithm
 */
@ExperimentalUnsignedTypes
open class CRC32Basic(private val poly: UInt, private val init: UInt) {
    private var crc = init

    fun reset() {
        crc = init
    }

    fun update(data: Byte) {
        var crc = this.crc.inv()
        crc = crc xor data.toUInt()
        for (k in 0 until 8)
            crc = if (crc and 1u > 0u) (crc shr 1) xor poly else crc shr 1
        this.crc = crc.inv()
    }

    fun update(data: ByteArray, offset: Int = 0, length: Int = data.size - offset) {
        var crc = this.crc.inv()
        for (i in offset until offset + length) {
            crc = crc xor data[i].toUInt()
            for (k in 0 until 8)
                crc = if (crc and 1u > 0u) (crc shr 1) xor poly else crc shr 1
        }
        this.crc = crc.inv()
    }

    fun update(data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset) {
        var crc = this.crc.inv()
        for (i in offset until offset + length) {
            crc = crc xor data[i].toUInt()
            for (k in 0 until 8)
                crc = if (crc and 1u > 0u) (crc shr 1) xor poly else crc shr 1
        }
        this.crc = crc.inv()
    }

    val value: UInt
        get() = crc
}