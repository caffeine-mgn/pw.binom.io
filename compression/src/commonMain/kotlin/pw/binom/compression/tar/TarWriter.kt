package pw.binom.compression.tar

import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.Closeable
import pw.binom.io.OutputStream

private val ZERO_BYTE = ByteArray(100) { 0 }


internal fun Int.forPart(partSize: Int): Int {
    var fullSize = (this / partSize) * partSize
    if (this % partSize > 0)
        fullSize += partSize
    return fullSize
}

private fun OutputStream.writeZero(size: Int) {
    var s = size
    while (s > 0) {
        val l = minOf(ZERO_BYTE.size, s)
        val b = write(ZERO_BYTE, 0, l)
        s -= b
        println("writed $b")
    }
}

internal fun UShort.toOct(dst: ByteArray, dstOffset: Int, size: Int) {
    this.toLong().toOct(dst, dstOffset, size)
}

internal fun UInt.toOct(dst: ByteArray, dstOffset: Int, size: Int) {
    this.toLong().toOct(dst, dstOffset, size)
}

internal fun Long.toOct(dst: ByteArray, dstOffset: Int, size: Int) {
    var n = this

    var len = 0
    while (n != 0L) {
        n = n shr 3
        len++
    }
    n = this

    (dstOffset until (dstOffset + size - len)).forEach {
        dst[it] = '0'.toByte()
    }
    var i = 0
    while (n != 0L) {
        val v = (n and 0x7).toByte()
        dst[dstOffset + (size - 2) - i] = (v + '0'.toByte()).toByte()
        n = n shr 3
        i++
    }
    dst[dstOffset + (size - 1)] = 0
}

private val magic = byteArrayOf(
        'u'.toByte(),
        's'.toByte(),
        't'.toByte(),
        'a'.toByte(),
        'r'.toByte(),
        0
)

private val version = byteArrayOf(
        '0'.toByte(),
        '0'.toByte()
)

@OptIn(ExperimentalStdlibApi::class)
private val longLink = "././@LongLink".encodeToByteArray()

class TarWriter(val stream: OutputStream) : Closeable {

    @OptIn(ExperimentalStdlibApi::class)
    fun newEntity(name: String, mode: UShort, uid: UShort, gid: UShort, time: Long, type: TarEntityType): OutputStream {

        val block = ByteArray(BLOCK_SIZE.toInt())

        val nameBytes = name.encodeToByteArray()
        if (nameBytes.size > 100) {
            longLink.copyInto(block)
            mode.toOct(block, 100, 8)
            uid.toOct(block, 108, 8)
            gid.toOct(block, 116, 8)
            time.toOct(block, 136, 12)

            block[156] = 76
            magic.copyInto(block, 257)
            version.copyInto(block, 263)
            (nameBytes.size + 1).toUInt().toOct(block, 124, 12)

            block.calcCheckSum().toOct(block, 148, 7)
            block[155] = ' '.toByte()
            stream.write(block)
            stream.write(nameBytes)
            var fullSize = (nameBytes.size + 1).forPart(BLOCK_SIZE.toInt())
            val needAddZero = fullSize - nameBytes.size
            stream.writeZero(needAddZero)
            block.fill(0.toByte())
            nameBytes.copyInto(block, 0, 0, 100)
        } else {
            nameBytes.copyInto(block)
        }

        mode.toOct(block, 100, 8)
        uid.toOct(block, 108, 8)
        gid.toOct(block, 116, 8)
        time.toOct(block, 136, 12)

        block[156] = type.num
        magic.copyInto(block, 257)
        version.copyInto(block, 263)

        return object : OutputStream {
            val data = ByteArrayOutputStream()

            override fun write(data: ByteArray, offset: Int, length: Int) =
                    this.data.write(data, offset, length)

            override fun flush() {

            }

            override fun close() {
                data.size.toUInt().toOct(block, 124, 12)

                block.calcCheckSum().toOct(block, 148, 7)
                block[155] = ' '.toByte()


                stream.write(block)
                stream.write(data.toByteArray())
                stream.writeZero(BLOCK_SIZE.toInt() - data.size % BLOCK_SIZE.toInt())
            }

        }
    }

    override fun close() {
        stream.writeZero(BLOCK_SIZE.toInt())
        stream.writeZero(BLOCK_SIZE.toInt())
    }

}

internal fun ByteArray.calcCheckSum(): UInt {
    var chksum = 0u
    forEach {
        chksum += it.toUInt()
    }
    chksum += 256u
    return chksum
}