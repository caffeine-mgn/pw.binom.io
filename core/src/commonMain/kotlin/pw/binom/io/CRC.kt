package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.dump

internal fun makeCrcTable(poly: UInt): IntArray {
    val crcTable = IntArray(256)
    repeat(256) { n ->
        var c = n
        repeat(8) {
            c = if (c and 1 != 0) {
                poly.toInt() xor (c ushr 1)
            } else {
                c ushr 1
            }
        }
        crcTable[n] = c
    }
    return crcTable
}

open class CRC32Basic(private val init: UInt, val crcTable: IntArray) : MessageDigest {
    private var crc = 0

    init {
        init()
    }

    override fun init() {
        crc = init.inv().toInt()
    }

    override fun update(byte: Byte) {
        crc = apply(byte, crc)
    }

    override fun update(input: ByteArray, offset: Int, len: Int) {
        for (i in offset until offset + len) {
            crc = apply(input[i], crc)
        }
    }

    inline fun apply(byte: Byte, c: Int): Int {
        val o = byte.toInt() and 0xFF
        return (c ushr 8) xor crcTable[o xor (c and 0xff)]
    }

    override fun update(buffer: ByteBuffer) {
        while (buffer.remaining > 0) {
            crc = apply(buffer.get(), crc)
        }
    }

    override fun finish(): ByteArray =
        value.toInt().dump()

    val value: UInt
        get() = crc.inv().toUInt()
}
