package pw.binom.uuid

import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
internal val digits = charArrayOf(
    '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', 'a', 'b',
    'c', 'd', 'e', 'f', 'g', 'h',
    'i', 'j', 'k', 'l', 'm', 'n',
    'o', 'p', 'q', 'r', 's', 't',
    'u', 'v', 'w', 'x', 'y', 'z'
)

internal fun Long.Companion.formatUnsignedLong0(value: Long, shift: Int, buf: CharArray, offset: Int, len: Int) {
    var charPos = offset + len
    val radix = 1 shl shift
    val mask = radix - 1
    var value2 = value
    do {
        buf[--charPos] = (digits[(value2.toInt()) and mask])
        value2 = value2 ushr shift
    } while (charPos > offset)
}

internal fun String.toLong(beginIndex: Int, endIndex: Int, radix: Int) =
    substring(beginIndex, endIndex).toLong(radix)

internal fun Long.toBytes(array: ByteArray, offset: Int = 0) {
    if (array.size - offset < Long.SIZE_BYTES) {
        throw IllegalArgumentException()
    }
    array[0 + offset] = ushr(56).toByte()
    array[1 + offset] = ushr(48).toByte()
    array[2 + offset] = ushr(40).toByte()
    array[3 + offset] = ushr(32).toByte()
    array[4 + offset] = ushr(24).toByte()
    array[5 + offset] = ushr(16).toByte()
    array[6 + offset] = ushr(8).toByte()
    array[7 + offset] = ushr(0).toByte()
}
