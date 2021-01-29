package pw.binom.dns

import pw.binom.*
import pw.binom.dns.protocol.DnsHeader
import pw.binom.dns.protocol.ResourcePackage

data class DnsRecord(
    /**
     * identification number
     */
    val id: Short,

    /**
     * recursion desired
     */
    val rd: Boolean,

    /**
     * truncated message
     */
    val tc: Boolean,

    /**
     * authoritive answer
     */
    val aa: Boolean,

    /**
     * purpose of message
     */
    val opcode: Byte,

    /**
     * query/response flag
     */
    val qr: Boolean,

    /**
     * recursion available
     */
    val ra: Boolean,

    /**
     * its z! reserved
     */
    val z: Boolean,

    /**
     * authenticated data
     */
    val ad: Boolean,

    /**
     * checking disabled
     */
    val cd: Boolean,

    /**
     * response code
     */
    val rcode: Byte,
    val queries: List<Query>,
    val ans: List<pw.binom.dns.Resource>,
    val auth: List<pw.binom.dns.Resource>,
    val add: List<pw.binom.dns.Resource>,
) {
    companion object {
        suspend fun read(input: AsyncInput, buffer: ByteBuffer): DnsRecord {
            buffer.reset(0, Short.SIZE_BYTES)
            input.readFully(buffer)
            val dnsPackageSize = buffer.readShort()
            if (buffer.capacity < dnsPackageSize) {
                throw IllegalArgumentException("Can't read full dns response to buffer. Package size: $dnsPackageSize, Buffer capacity: ${buffer.capacity}")
            }
            buffer.reset(0, dnsPackageSize.toInt())
            input.readFully(buffer)
            read(buffer)
            return read(buffer)
        }

        /**
         * Read dns record fully. Without first size (2 bytes)
         */
        fun read(src: ByteBuffer): DnsRecord {
            val header = DnsHeader()
            header.readPackage(src)
            val query = pw.binom.dns.protocol.QueryPackage()
            val r = ResourcePackage()
            val queries = (0 until header.q_count.toInt()).map {
                query.read(src).toImmutable()
            }
            val ans = (0 until header.ans_count.toInt()).map {
                r.read(src).toImmutable()
            }
            val auth = (0 until header.auth_count.toInt()).map {
                r.read(src).toImmutable()
            }
            val add = (0 until header.add_count.toInt()).map {
                r.read(src).toImmutable()
            }
            return DnsRecord(
                id = header.id,
                rd = header.rd,
                tc = header.tc,
                aa = header.aa,
                opcode = header.opcode,
                qr = header.qr,
                ra = header.ra,
                z = header.z,
                ad = header.ad,
                cd = header.cd,
                rcode = header.rcode,
                queries = queries,
                ans = ans,
                auth = auth,
                add = add
            )
        }
    }

    suspend fun write(output: AsyncOutput, buffer: ByteBuffer): Int {
        buffer.clear()
        buffer.writeShort(0)
        write(buffer)
        val l = buffer.position
        buffer.position = 0
        buffer.writeShort((l - Short.SIZE_BYTES).toShort())
        buffer.position = l
        buffer.flip()
        println("${buffer.remaining}")
        return output.write(buffer)
    }

    /**
     * Write full dns query without first size (first 2 bytes)
     */
    fun write(dest: ByteBuffer) {
        val header = DnsHeader()
        header.id = id
        header.rd = rd
        header.tc = tc
        header.aa = aa
        header.opcode = opcode
        header.qr = qr
        header.ra = ra
        header.z = z
        header.ad = ad
        header.cd = cd
        header.q_count = queries.size.toUShort()
        header.ans_count = ans.size.toUShort()
        header.auth_count = auth.size.toUShort()
        header.add_count = add.size.toUShort()
        header.write(dest)
        val q = pw.binom.dns.protocol.QueryPackage()
        val r = ResourcePackage()
        queries.forEach {
            it.toMutable(q)
            q.write(dest)
        }
        ans.forEach {
            it.toMutable(r)
            r.write(dest)
        }
        auth.forEach {
            it.toMutable(r)
            r.write(dest)
        }
        add.forEach {
            it.toMutable(r)
            r.write(dest)
        }
    }
}