package pw.binom.io

/**
 * https://stackoverflow.com/questions/27939882/fast-crc-algorithm
 */
class CRC32 {
    private var crc = 0u


    fun update(data: ByteArray, offset: Int = 0, length: Int = data.size - offset) {

        var crc = this.crc.inv()

        val POLY = 0xedb88320.toUInt()

        for (i in offset until offset + length) {
            crc = crc xor data[i].toUInt()
            for (k in 0 until 8)
                crc = if (crc and 1u > 0u) (crc shr 1) xor POLY else crc shr 1
        }
        this.crc = crc.inv()
    }

    val value: UInt
        get() = crc
}