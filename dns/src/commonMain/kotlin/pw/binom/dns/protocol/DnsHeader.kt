package pw.binom.dns.protocol

import pw.binom.BitArray32
import pw.binom.dns.Header
import pw.binom.dns.Opcode
import pw.binom.dns.Rcode
import pw.binom.io.AsyncInput
import pw.binom.io.ByteBuffer
import pw.binom.readShort
import pw.binom.writeShort

class DnsHeader {
    val header = Header()

    /**
     * identification number
     */
    val id: Short
        get() = header.id

    /**
     * recursion desired
     */
    val rd: Boolean
        get() = header.rd

    /**
     * truncated message
     */
    val tc: Boolean
        get() = header.tc

    /**
     * authoritive answer
     */
    val aa: Boolean
        get() = header.aa

    /**
     * purpose of message
     */
    val opcode: Opcode
        get() = header.opcode

    /**
     * query/response flag
     */
    val qr: Boolean
        get() = header.qr

    /**
     * recursion available
     */
    val ra: Boolean
        get() = header.ra

    /**
     * its z! reserved
     */
    val z: Boolean
        get() = header.z

    /**
     * authenticated data
     */
    val ad: Boolean
        get() = header.ad

    /**
     * checking disabled
     */
    val cd: Boolean
        get() = header.cd

    /**
     * response code
     */
    val rcode: Rcode
        get() = header.rcode

    /**
     * number of question entries
     */
    var qCount: UShort = 0u

    /**
     * number of answer entries
     */
    var ansCount: UShort = 0u

    /**
     * number of authority entries
     */
    var authCount: UShort = 0u

    /**
     * number of resource entries
     */
    var addCount: UShort = 0u

    suspend fun read(input: AsyncInput, buf: ByteBuffer) {
        val len = input.readShort(buf)
        require(buf.capacity >= len) { "Buffer Capacity must be more than $len" }
        buf.clear()
        buf.reset(0, len.toInt())
        input.readFully(buf)
        buf.flip()
        readPackage(buf)
    }

    fun readPackage(buf: ByteBuffer) {
        header.read(buf)
        qCount = buf.readShort().toUShort()
        ansCount = buf.readShort().toUShort()
        authCount = buf.readShort().toUShort()
        addCount = buf.readShort().toUShort()
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
            .updateByte4(1 + Short.SIZE_BITS, opcode.raw)
            .update(5 + Short.SIZE_BITS, aa)
            .update(6 + Short.SIZE_BITS, tc)
            .update(7 + Short.SIZE_BITS, rd)
            .update(8 + Short.SIZE_BITS, ra)
            .update(9 + Short.SIZE_BITS, z)
            .update(10 + Short.SIZE_BITS, ad)
            .update(11 + Short.SIZE_BITS, cd)
            .updateByte4(12 + Short.SIZE_BITS, rcode.raw)
            .toInt()
            .let { it and 0xFFF }
            .toShort()
        dest.writeShort(flags)
        dest.writeShort(qCount.toShort())
        dest.writeShort(ansCount.toShort())
        dest.writeShort(authCount.toShort())
        dest.writeShort(addCount.toShort())
    }

    fun writeEnd(packageStartPosition: Int, dest: ByteBuffer) {
        val packageEndPosition = dest.position
        dest.position = packageStartPosition
        dest.writeShort((packageStartPosition - packageEndPosition - Short.SIZE_BYTES).toShort())
        dest.reset(0, packageEndPosition)
    }

    override fun toString(): String {
        val id = id.toUShort().toString(16)
        return "DnsHeader(id=0x$id, qr=$qr, opcode=$opcode, aa=$aa, tc=$tc, rd=$rd, ra=$ra, z=$z, ad=$ad, cd=$cd, rcode=$rcode, q_count=$qCount, ans_count=$ansCount, auth_count=$authCount, add_count=$addCount)"
    }
}
