package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.dump

internal fun make_crc_table(poly: UInt): IntArray {
    val crc_table = IntArray(256)
    repeat(256) { n ->
        var c = n
        repeat(8) {
            c = if (c and 1 != 0) {
//                -306674912 xor (c ushr 1)
                poly.toInt() xor (c ushr 1)
            } else {
                c ushr 1
            }
        }
        crc_table[n] = c
    }
//    repeat(256) { n ->
//        var c = n;
//
////        for (k in 8 downTo 0) {
//        var k = 8
//        repeat(8) {
//            k--
//            println("$k flag=${(c and 1) != 0} last C=$c")
//            c = if ((c and 1) != 0)
//                poly.toInt() xor (c ushr 1)
//            else
//                c ushr 1
//        }
//
//        println("Result $n: $c")
//        crc_table[n] = c;
//    }
    return crc_table
}

/**
 * https://stackoverflow.com/questions/27939882/fast-crc-algorithm
 */
open class CRC32Basic(private val poly: UInt, private val init: UInt, val crc_table: IntArray) : MessageDigest {
    private var crc = 0

    init {
        init()
    }

    override fun init() {
        crc = init.inv().toInt()
    }

    override fun update(data: Byte) {
        crc = apply(data, crc)
    }

    override fun update(data: ByteArray, offset: Int, length: Int) {
        for (i in offset until offset + length) {
            crc = apply(data[i], crc)
        }
    }

    inline fun apply(byte: Byte, c: Int): Int {
        val o = byte.toInt() and 0xFF
        return (c ushr 8) xor crc_table[o xor (c and 0xff)]
    }

    override fun update(data: ByteBuffer) {
        while (data.remaining > 0) {
            crc = apply(data.get(), crc)
        }
    }

    override fun finish(): ByteArray =
        value.toInt().dump()

    val value: UInt
        get() = crc.inv().toUInt()
}