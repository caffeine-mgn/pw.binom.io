package pw.binom.dns

import pw.binom.dns.protocol.ResourcePackage
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.readShort
import pw.binom.writeShort

data class DnsRecord(
    /*
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
    */
    val header: Header,
    val queries: List<Query>,
    val ans: List<Resource>,
    val auth: List<Resource>,
    val add: List<Resource>,
) {
    companion object {
        suspend fun read(input: AsyncInput, buffer: ByteBuffer): DnsRecord {
            buffer.reset(0, Short.SIZE_BYTES)
            input.readFully(buffer)
            buffer.flip()
            val dnsPackageSize = buffer.readShort()
            if (buffer.capacity < dnsPackageSize) {
                throw IllegalArgumentException("Can't read full dns response to buffer. Package size: $dnsPackageSize, Buffer capacity: ${buffer.capacity}")
            }
            buffer.reset(0, dnsPackageSize.toInt())
            input.readFully(buffer)
            buffer.flip()
            println("DnsRecord:: buffer -> position: ${buffer.position} limit: ${buffer.limit} remaining: ${buffer.remaining} capacity: ${buffer.capacity}")
            return read(buffer)
        }

        /**
         * Read dns record fully. Without first size (2 bytes)
         */
        fun read(src: ByteBuffer): DnsRecord {
            val header = Header()
            header.read(src)
            var qCount = 0
            var ansCount = 0
            var authCount = 0
            var addCount = 0
            PayloadCounters.read(
                buffer = src,
                qCount = { qCount = it.toInt() },
                ansCount = { ansCount = it.toInt() },
                authCount = { authCount = it.toInt() },
                addCount = { addCount = it.toInt() },
            )
            val query = pw.binom.dns.protocol.QueryPackage()
            val r = ResourcePackage()
            val queries = (0 until qCount.toInt()).map {
                query.read(src).toImmutable()
            }
            val ans = (0 until ansCount.toInt()).map {
                r.read(src).toImmutable()
            }
            val auth = (0 until authCount.toInt()).map {
                r.read(src).toImmutable()
            }
            val add = (0 until addCount.toInt()).map {
                r.read(src).toImmutable()
            }
            return DnsRecord(
                header = header,
                queries = queries,
                ans = ans,
                auth = auth,
                add = add,
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
        return output.write(buffer)
    }

    /**
     * Write full dns query without first size (first 2 bytes)
     */
    fun write(dest: ByteBuffer) {
        header.write(buffer = dest)
        PayloadCounters.write(
            qCount = queries.size,
            ansCount = ans.size,
            authCount = auth.size,
            addCount = add.size,
            buffer = dest,
        )
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
