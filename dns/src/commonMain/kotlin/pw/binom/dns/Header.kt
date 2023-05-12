package pw.binom.dns

import pw.binom.BitArray32
import pw.binom.io.*
import pw.binom.readShort
import pw.binom.toBitset
import pw.binom.writeShort

class Header {

    companion object {
        const val SIZE_BYTES = Short.SIZE_BYTES * 2
    }

    /**
     * identification number
     */
    var id: Short = 0

    /**
     * recursion desired
     */
    var rd: Boolean = false

    /**
     * truncated message
     */
    var tc: Boolean = false

    /**
     * authoritive answer
     */
    var aa: Boolean = false

    /**
     * purpose of message
     */
    var opcode: Byte = 0

    /**
     * query/response flag
     */
    var qr: Boolean = false

    /**
     * recursion available
     */
    var ra: Boolean = false

    /**
     * its z! reserved
     */
    var z: Boolean = false

    /**
     * authenticated data
     */
    var ad: Boolean = false

    /**
     * checking disabled
     */
    var cd: Boolean = false

    /**
     * response code
     */
    var rcode: Byte = 0

    private fun setFlags(value: Short) {
        val flags = (value.toInt() and 0xFFFF).toBitset()
        qr = flags[0 + Short.SIZE_BITS]
        opcode = flags.getByte4(1 + Short.SIZE_BITS)
        aa = flags[5 + Short.SIZE_BITS]
        tc = flags[6 + Short.SIZE_BITS]
        rd = flags[7 + Short.SIZE_BITS]
        ra = flags[8 + Short.SIZE_BITS]
        z = flags[9 + Short.SIZE_BITS]
        ad = flags[10 + Short.SIZE_BITS]
        cd = flags[11 + Short.SIZE_BITS]
        rcode = flags.getByte4(12 + Short.SIZE_BITS)
    }

    fun read(buffer: ByteBuffer) {
        id = buffer.readShort()
        setFlags(buffer.readShort())
    }

    suspend fun read(input: AsyncInput, buffer: ByteBuffer) {
        id = input.readShort(buffer)
        setFlags(input.readShort(buffer))
    }

    fun read(input: Input, buffer: ByteBuffer) {
        id = input.readShort(buffer)
        setFlags(input.readShort(buffer))
    }

    private fun getFlags() = BitArray32().update(0 + Short.SIZE_BITS, qr).updateByte4(1 + Short.SIZE_BITS, opcode)
        .update(5 + Short.SIZE_BITS, aa).update(6 + Short.SIZE_BITS, tc).update(7 + Short.SIZE_BITS, rd)
        .update(8 + Short.SIZE_BITS, ra).update(9 + Short.SIZE_BITS, z).update(10 + Short.SIZE_BITS, ad)
        .update(11 + Short.SIZE_BITS, cd).updateByte4(12 + Short.SIZE_BITS, rcode).toInt().let { it and 0xFFF }
        .toShort()

    suspend fun write(output: AsyncOutput, buffer: ByteBuffer) {
        output.writeShort(value = id, buffer = buffer)
        output.writeShort(value = getFlags(), buffer = buffer)
    }

    fun write(output: Output, buffer: ByteBuffer) {
        output.writeShort(value = id, buffer = buffer)
        output.writeShort(value = getFlags(), buffer = buffer)
    }

    fun write(buffer: ByteBuffer) {
        buffer.writeShort(id)
        buffer.writeShort(getFlags())
    }
}
