package pw.binom.dns

import pw.binom.dns.protocol.QueryPackage
import pw.binom.dns.protocol.ResourcePackage
import pw.binom.io.*
import pw.binom.readShort
import pw.binom.writeShort

/*
 *    +---------------------+
 *    |        Header       |
 *    +---------------------+
 *    |       Question      | the question for the name server
 *    +---------------------+
 *    |        Answer       | RRs answering the question
 *    +---------------------+
 *    |      Authority      | RRs pointing toward an authority
 *    +---------------------+
 *    |      Additional     | RRs holding additional information
 *    +---------------------+
 */

data class DnsPackage(
    val header: Header,
    val question: List<QueryPackage>,
    val answer: List<ResourcePackage>,
    val authority: List<ResourcePackage>,
    val additional: List<ResourcePackage>,
) {
    val sizeBytes
        get() = Header.SIZE_BYTES +
            PayloadCounters.SIZE_BYTES +
            question.sumOf { it.sizeBytes } +
            answer.sumOf { it.sizeBytes } +
            authority.sumOf { it.sizeBytes } +
            additional.sumOf { it.sizeBytes }

    companion object {
        fun readWithoutSize(buffer: ByteBuffer): DnsPackage {
            val header = Header()
            header.read(buffer)
            val question = ArrayList<QueryPackage>()
            val answer = ArrayList<ResourcePackage>()
            val authority = ArrayList<ResourcePackage>()
            val additional = ArrayList<ResourcePackage>()
            var qCount = 0
            var ansCount = 0
            var authCount = 0
            var addCount = 0
            PayloadCounters.read(
                buffer = buffer,
                qCount = { qCount = it.toInt() },
                ansCount = { ansCount = it.toInt() },
                authCount = { authCount = it.toInt() },
                addCount = { addCount = it.toInt() },
            )
            repeat(qCount) {
                val q = QueryPackage()
                q.read(buffer)
                question += q
            }
            repeat(ansCount) {
                val r = ResourcePackage()
                r.read(buffer)
                answer += r
            }
            repeat(authCount) {
                val r = ResourcePackage()
                r.read(buffer)
                authority += r
            }
            repeat(addCount) {
                val r = ResourcePackage()
                r.read(buffer)
                additional += r
            }

            return DnsPackage(
                header = header,
                question = question,
                answer = answer,
                authority = authority,
                additional = additional,
            )
        }

        suspend fun read(input: AsyncInput, buffer: ByteBuffer): DnsPackage {
            val len = input.readShort(buffer).toInt()
            buffer.reset(0, len)
            input.readFully(buffer)
            buffer.flip()
            return readWithoutSize(buffer)
        }

        fun read(input: Input, buffer: ByteBuffer): DnsPackage {
            val len = input.readShort(buffer).toInt()
            buffer.reset(0, len)
            input.readFully(buffer)
            buffer.flip()
            return readWithoutSize(buffer)
        }
    }

    suspend fun write(output: AsyncOutput, buffer: ByteBuffer) {
        buffer.clear()
        buffer.writeShort(sizeBytes.toShort())
        createBuffer(buffer)
        buffer.flip()
        output.writeFully(buffer)
    }

    suspend fun write(output: AsyncOutput) =
        ByteBuffer(sizeBytes + Short.SIZE_BYTES).use { buffer ->
            write(output = output, buffer = buffer)
        }

    fun write(output: Output) =
        ByteBuffer(sizeBytes + Short.SIZE_BYTES).use { buffer ->
            write(output = output, buffer = buffer)
        }

    fun write(output: Output, buffer: ByteBuffer) {
        buffer.clear()
        buffer.writeShort(sizeBytes.toShort())
        createBuffer(buffer)
        buffer.flip()
        output.writeFully(buffer)
    }

    fun createBuffer(buffer: ByteBuffer) {
        header.write(buffer)
        PayloadCounters.write(
            buffer = buffer,
            qCount = question.size,
            ansCount = answer.size,
            authCount = authority.size,
            addCount = additional.size,
        )
        question.forEach {
            it.write(buffer)
        }
        answer.forEach {
            it.write(buffer)
        }
        authority.forEach {
            it.write(buffer)
        }
        additional.forEach {
            it.write(buffer)
        }
    }
}
