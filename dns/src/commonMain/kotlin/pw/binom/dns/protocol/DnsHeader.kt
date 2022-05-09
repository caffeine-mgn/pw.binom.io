package pw.binom.dns.protocol

import pw.binom.*

class DnsHeader {
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

    /**
     * number of question entries
     */
    var q_count: UShort = 0u

    /**
     * number of answer entries
     */
    var ans_count: UShort = 0u

    /**
     * number of authority entries
     */
    var auth_count: UShort = 0u

    /**
     * number of resource entries
     */
    var add_count: UShort = 0u

    suspend fun read(input: AsyncInput, buf: ByteBuffer) {
        val len = input.readShort(buf)
        if (buf.capacity < len) {
            throw IllegalArgumentException("Buffer Capacity must be more than $len")
        }
        buf.clear()
        buf.reset(0, len.toInt())
        input.readFully(buf)
        buf.flip()

        readPackage(buf)
    }

    fun readPackage(buf: ByteBuffer) {
        id = buf.readShort()
        val flags = (buf.readShort().toInt() and 0xFFFF).toBitset()
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
        q_count = buf.readShort().toUShort()
        ans_count = buf.readShort().toUShort()
        auth_count = buf.readShort().toUShort()
        add_count = buf.readShort().toUShort()
    }

    fun writeStart(dest: ByteBuffer): Int {
        val packageStartPosition = dest.position
        dest.writeShort(0)
        write(dest)
        return packageStartPosition
    }

    fun write(dest: ByteBuffer) {
        dest.writeShort(id)
        val flags = BitArray32()
            .update(0 + Short.SIZE_BITS, qr)
            .updateByte4(1 + Short.SIZE_BITS, opcode)
            .update(5 + Short.SIZE_BITS, aa)
            .update(6 + Short.SIZE_BITS, tc)
            .update(7 + Short.SIZE_BITS, rd)
            .update(8 + Short.SIZE_BITS, ra)
            .update(9 + Short.SIZE_BITS, z)
            .update(10 + Short.SIZE_BITS, ad)
            .update(11 + Short.SIZE_BITS, cd)
            .updateByte4(12 + Short.SIZE_BITS, rcode).toInt().let { it and 0xFFF }.toShort()
        dest.writeShort(flags)
        dest.writeShort(q_count.toShort())
        dest.writeShort(ans_count.toShort())
        dest.writeShort(auth_count.toShort())
        dest.writeShort(add_count.toShort())
    }

    fun writeEnd(packageStartPosition: Int, dest: ByteBuffer) {
        val packageEndPosition = dest.position
        dest.position = packageStartPosition
        dest.writeShort((packageStartPosition - packageEndPosition - Short.SIZE_BYTES).toShort())
        dest.reset(0, packageEndPosition)
    }

    override fun toString(): String {
        return "DnsHeader(id=0x${
        id.toUShort().toString(16)
        }, qr=$qr, opcode=$opcode, aa=$aa, tc=$tc, rd=$rd, ra=$ra, z=$z, ad=$ad, cd=$cd, rcode=$rcode, q_count=$q_count, ans_count=$ans_count, auth_count=$auth_count, add_count=$add_count)"
    }
}
