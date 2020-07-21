package pw.binom

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.random.Random

class UUID(val mostSigBits: Long, val leastSigBits: Long) {

    companion object {
        fun create(data: ByteArray): UUID {
            if (data.size != 16)
                throw IllegalArgumentException("data must be 16 bytes in length")
            var msb: Long = 0
            var lsb: Long = 0
            for (i in 0..7) msb = msb shl 8 or (data[i].toLong() and 0xff)
            for (i in 8..15) lsb = lsb shl 8 or (data[i].toLong() and 0xff)
            val mostSigBits = msb
            val leastSigBits = lsb
            return UUID(
                    mostSigBits = mostSigBits,
                    leastSigBits = leastSigBits
            )
        }

        fun random(): UUID {
            val randomBytes = Random.nextBytes(16)
            randomBytes[6] = randomBytes[6] and 0x0f
            randomBytes[6] = randomBytes[6] or 0x40
            randomBytes[8] = randomBytes[8] and 0x3f
            randomBytes[8] = randomBytes[8] or 0x80.toByte()
            return create(randomBytes)
        }

        fun fromString(uuid: String): UUID {
            val len = uuid.length
            require(len <= 36) { "UUID string too large" }

            val dash1 = uuid.indexOf('-', 0)
            val dash2 = uuid.indexOf('-', dash1 + 1)
            val dash3 = uuid.indexOf('-', dash2 + 1)
            val dash4 = uuid.indexOf('-', dash3 + 1)
            val dash5 = uuid.indexOf('-', dash4 + 1)

            require(!(dash4 < 0 || dash5 >= 0)) { "Invalid UUID string: $uuid" }

            var mostSigBits: Long = uuid.toLong(0, dash1, 16) and 0xffffffffL
            mostSigBits = mostSigBits shl 16
            mostSigBits = mostSigBits or (uuid.toLong(dash1 + 1, dash2, 16) and 0xffffL)
            mostSigBits = mostSigBits shl 16
            mostSigBits = mostSigBits or (uuid.toLong(dash2 + 1, dash3, 16) and 0xffffL)
            var leastSigBits: Long = uuid.toLong(dash3 + 1, dash4, 16) and 0xffffL
            leastSigBits = leastSigBits shl 48
            leastSigBits = leastSigBits or (uuid.toLong(dash4 + 1, len, 16) and 0xffffffffffffL)
            return UUID(
                    mostSigBits = mostSigBits,
                    leastSigBits = leastSigBits
            )
        }
    }

    fun toShortString(): String {
        val buf = CharArray(8)
        val msb = mostSigBits
        Long.formatUnsignedLong0(msb ushr 32, 4, buf, 0, 8)
        return String(buf)
    }

    fun toByteArray(output: ByteArray) {
        if (output.size < 16)
            throw IllegalArgumentException()
        mostSigBits.toBytes(output, 0)
        leastSigBits.toBytes(output, 8)
    }

    override fun toString(): String {
        val lsb = leastSigBits
        val msb = mostSigBits
        val buf = CharArray(36)
        Long.formatUnsignedLong0(lsb, 4, buf, 24, 12)
        Long.formatUnsignedLong0(lsb ushr 48, 4, buf, 19, 4)
        Long.formatUnsignedLong0(msb, 4, buf, 14, 4)
        Long.formatUnsignedLong0(msb ushr 16, 4, buf, 9, 4)
        Long.formatUnsignedLong0(msb ushr 32, 4, buf, 0, 8)

        buf[23] = '-'
        buf[18] = '-'
        buf[13] = '-'
        buf[8] = '-'

        return String(buf)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as UUID

        if (mostSigBits != other.mostSigBits) return false
        if (leastSigBits != other.leastSigBits) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mostSigBits.hashCode()
        result = 31 * result + leastSigBits.hashCode()
        return result
    }
}

private val digits = arrayOf(
        '0', '1', '2', '3', '4', '5',
        '6', '7', '8', '9', 'a', 'b',
        'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n',
        'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z')

private fun Long.Companion.formatUnsignedLong0(value: Long, shift: Int, buf: CharArray, offset: Int, len: Int) {
    var charPos = offset + len;
    val radix = 1 shl shift
    val mask = radix - 1;
    var value = value
    do {
        buf[--charPos] = (digits[(value.toInt()) and mask]).toByte().toChar()
        value = value ushr shift;
    } while (charPos > offset);
}

private fun String.toLong(beginIndex: Int, endIndex: Int, radix: Int) =
        substring(beginIndex, endIndex).toLong(radix)

inline fun Random.uuid() = UUID.random()